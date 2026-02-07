package com.example.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.domain.InventoryMovement;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.InventoryMovementRepository;
import com.example.ecommerce.repository.ProductRepository;

@Service
public class InventoryMovementService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryMovementService(
            ProductRepository productRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found for id=" + productId);
        }

        return inventoryMovementRepository.findByProductIdOrderByCreatedAtAsc(productId);
    }
}
