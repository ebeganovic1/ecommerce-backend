package com.example.ecommerce.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.api.dto.CheckoutResponse;
import com.example.ecommerce.service.CheckoutService;

@RestController
@RequestMapping("/api/carts")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable Long cartId) {
        Long orderId = checkoutService.checkout(cartId);
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
