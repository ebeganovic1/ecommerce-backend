package com.example.ecommerce.api.dto;

import java.time.OffsetDateTime;

import com.example.ecommerce.domain.InventoryMovement;
import com.example.ecommerce.domain.InventoryMovementType;

public class InventoryMovementResponse {

    private final Long id;
    private final InventoryMovementType type;
    private final Integer quantityDelta;
    private final Long relatedOrderId;
    private final OffsetDateTime createdAt;

    public InventoryMovementResponse(InventoryMovement m) {
        this.id = m.getId();
        this.type = m.getType();
        this.quantityDelta = m.getQuantityDelta();
        this.relatedOrderId = m.getRelatedOrder() != null ? m.getRelatedOrder().getId() : null;
        this.createdAt = m.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public InventoryMovementType getType() {
        return type;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
