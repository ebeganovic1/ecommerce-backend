package com.example.ecommerce.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.OrderItem;
import com.example.ecommerce.domain.OrderStatus;
import com.example.ecommerce.domain.OrderStatusHistory;
import com.example.ecommerce.domain.Payment;
import com.example.ecommerce.domain.PaymentStatus;
import com.example.ecommerce.exception.BusinessRuleException;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.OrderStatusHistoryRepository;
import com.example.ecommerce.repository.PaymentRepository;

@Service
public class PaymentService {

    public enum PaymentOutcome {
        SUCCESS,
        FAIL
    }

    public record PaymentResult(Long orderId, String idempotencyKey, PaymentOutcome outcome, String status) {
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryService inventoryService;
    private final PaymentRepository paymentRepository;

    public PaymentService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            InventoryService inventoryService,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.inventoryService = inventoryService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResult processPayment(Long orderId, String idempotencyKey, PaymentOutcome outcome) {
        if (orderId == null) {
            throw new BusinessRuleException("orderId must not be null");
        }
        if (outcome == null) {
            throw new BusinessRuleException("outcome must not be null");
        }
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BusinessRuleException("idempotencyKey must not be blank");
        }

        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            return new PaymentResult(
                    payment.getOrder().getId(),
                    payment.getIdempotencyKey(),
                    toOutcome(payment.getStatus()),
                    "REPLAYED"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found for id=" + orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new BusinessRuleException("Order has no items");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(toPaymentStatus(outcome));
        payment.setCreatedAt(OffsetDateTime.now());

        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            Optional<Payment> replayed = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (replayed.isPresent()) {
                Payment existingPayment = replayed.get();
                return new PaymentResult(
                        existingPayment.getOrder().getId(),
                        existingPayment.getIdempotencyKey(),
                        toOutcome(existingPayment.getStatus()),
                        "REPLAYED"
                );
            }
            throw ex;
        }

        OrderStatus fromStatus = order.getStatus();
        OrderStatus toStatus = outcome == PaymentOutcome.SUCCESS ? OrderStatus.PAID : OrderStatus.CANCELLED;
        order.setStatus(toStatus);
        orderRepository.save(order);

        if (outcome == PaymentOutcome.SUCCESS) {
            for (OrderItem item : items) {
                inventoryService.commitDeduct(item.getProduct().getId(), item.getQuantity(), orderId);
            }
        } else {
            for (OrderItem item : items) {
                inventoryService.release(item.getProduct().getId(), item.getQuantity(), orderId);
            }
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus != null ? fromStatus.name() : null);
        history.setToStatus(toStatus.name());
        history.setChangedAt(OffsetDateTime.now());
        history.setCreatedAt(OffsetDateTime.now());
        orderStatusHistoryRepository.save(history);

        return new PaymentResult(order.getId(), idempotencyKey, outcome, "APPLIED");
    }

    private PaymentStatus toPaymentStatus(PaymentOutcome outcome) {
        return outcome == PaymentOutcome.SUCCESS ? PaymentStatus.SUCCESS : PaymentStatus.FAIL;
    }

    private PaymentOutcome toOutcome(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS ? PaymentOutcome.SUCCESS : PaymentOutcome.FAIL;
    }
}
