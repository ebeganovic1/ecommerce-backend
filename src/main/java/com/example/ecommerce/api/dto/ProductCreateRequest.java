package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductCreateRequest {

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
