package com.example.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.domain.OrderStatusHistory;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.OrderStatusHistoryRepository;

@Service
public class OrderHistoryService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderHistoryService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrderHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Order not found for id=" + orderId);
        }

        return orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
    }
}
