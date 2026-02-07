package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentCreateRequest {

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String outcome;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
