package com.example.ecommerce.api.dto;

import java.time.OffsetDateTime;

import com.example.ecommerce.domain.OrderStatusHistory;

public class HistoryEntryResponse {

    private final String toStatus;
    private final OffsetDateTime changedAt;

    public HistoryEntryResponse(OrderStatusHistory history) {
        this.toStatus = history.getToStatus();
        this.changedAt = history.getChangedAt();
    }

    public String getToStatus() {
        return toStatus;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }
}
