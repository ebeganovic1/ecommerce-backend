package com.example.ecommerce.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.api.dto.PaymentCreateRequest;
import com.example.ecommerce.api.dto.PaymentResponse;
import com.example.ecommerce.exception.BusinessRuleException;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.PaymentService.PaymentOutcome;
import com.example.ecommerce.service.PaymentService.PaymentResult;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentOutcome outcome;
        try {
            outcome = PaymentOutcome.valueOf(request.getOutcome());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid payment outcome");
        }
        PaymentResult result = paymentService.processPayment(orderId, request.getIdempotencyKey(), outcome);

        PaymentResponse response = new PaymentResponse();
        response.setOrderId(result.orderId());
        response.setStatus(result.outcome().name());
        response.setReplayed("REPLAYED".equals(result.status()));
        return ResponseEntity.ok(response);
    }
}
