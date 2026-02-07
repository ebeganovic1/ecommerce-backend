package com.example.ecommerce.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.domain.Cart;
import com.example.ecommerce.domain.CartItem;
import com.example.ecommerce.domain.CartStatus;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.OrderItem;
import com.example.ecommerce.domain.OrderStatus;
import com.example.ecommerce.domain.OrderStatusHistory;
import com.example.ecommerce.exception.BusinessRuleException;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.OrderStatusHistoryRepository;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryService inventoryService;

    public CheckoutService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            InventoryService inventoryService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Long checkout(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found for id=" + cartId));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessRuleException("Cart is not ACTIVE");
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setCart(cart);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(OffsetDateTime.now());
        order = orderRepository.save(order);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException("Cart has no items");
        }

        for (CartItem item : cartItems) {
            inventoryService.reserve(item.getProduct().getId(), item.getQuantity(), order.getId());
        }

        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setCreatedAt(OffsetDateTime.now());
            orderItemRepository.save(orderItem);
        }

        cart.setStatus(CartStatus.CHECKED_OUT);
        cart.setUpdatedAt(OffsetDateTime.now());
        cartRepository.save(cart);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.CREATED.name());
        history.setChangedAt(OffsetDateTime.now());
        history.setCreatedAt(OffsetDateTime.now());
        orderStatusHistoryRepository.save(history);

        return order.getId();
    }
}
