package com.example.ecommerce.api.dto;

public class PaymentResponse {

    private Long orderId;
    private String status;
    private Boolean replayed;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getReplayed() {
        return replayed;
    }

    public void setReplayed(Boolean replayed) {
        this.replayed = replayed;
    }
}
