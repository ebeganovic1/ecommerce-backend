package com.example.ecommerce.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.api.dto.HistoryEntryResponse;
import com.example.ecommerce.service.OrderHistoryService;

@RestController
@RequestMapping("/api/orders")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<HistoryEntryResponse>> getOrderHistory(@PathVariable Long orderId) {
        List<HistoryEntryResponse> response = orderHistoryService.getOrderHistory(orderId).stream()
                .map(HistoryEntryResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
