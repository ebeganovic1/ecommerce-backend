package com.example.ecommerce.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.domain.Inventory;
import com.example.ecommerce.domain.InventoryMovement;
import com.example.ecommerce.domain.InventoryMovementType;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.exception.BusinessRuleException;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.InventoryMovementRepository;
import com.example.ecommerce.repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional
    public void reserve(Long productId, int qty, Long orderIdNullable) {
        if (qty <= 0) {
            throw new BusinessRuleException("Quantity must be greater than zero");
        }

        Inventory inventory = loadInventoryForUpdate(productId);

        if (inventory.getQuantity() < qty) {
            throw new BusinessRuleException("Insufficient stock");
        }

        inventory.setQuantity(inventory.getQuantity() - qty);
        inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(inventory.getProduct());
        movement.setType(InventoryMovementType.STOCK_OUT);
        movement.setQuantityDelta(-qty);
        movement.setRelatedOrder(orderRef(orderIdNullable));
        movement.setCreatedAt(OffsetDateTime.now());
        inventoryMovementRepository.save(movement);
    }

    @Transactional
    public void release(Long productId, int qty, Long orderIdNullable) {
        if (qty <= 0) {
            throw new BusinessRuleException("Quantity must be greater than zero");
        }

        Inventory inventory = loadInventoryForUpdate(productId);
        inventory.setQuantity(inventory.getQuantity() + qty);
        inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(inventory.getProduct());
        movement.setType(InventoryMovementType.STOCK_IN);
        movement.setQuantityDelta(qty);
        movement.setRelatedOrder(orderRef(orderIdNullable));
        movement.setCreatedAt(OffsetDateTime.now());
        inventoryMovementRepository.save(movement);
    }

    @Transactional
    public void commitDeduct(Long productId, int qty, Long orderId) {
        if (qty <= 0) {
            throw new BusinessRuleException("Quantity must be greater than zero");
        }
        if (orderId == null) {
            throw new BusinessRuleException("orderId must not be null");
        }
    }

    private Inventory loadInventoryForUpdate(Long productId) {
        return inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for productId=" + productId));
    }

    private Order orderRef(Long orderId) {
        if (orderId == null) {
            return null;
        }
        Order order = new Order();
        order.setId(orderId);
        return order;
    }
}
