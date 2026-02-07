package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotNull;

public class CartCreateRequest {

    @NotNull
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
