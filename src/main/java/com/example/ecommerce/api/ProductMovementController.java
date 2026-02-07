package com.example.ecommerce.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.api.dto.InventoryMovementResponse;
import com.example.ecommerce.service.InventoryMovementService;

@RestController
@RequestMapping("/api/products")
public class ProductMovementController {

    private final InventoryMovementService inventoryMovementService;

    public ProductMovementController(InventoryMovementService inventoryMovementService) {
        this.inventoryMovementService = inventoryMovementService;
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<InventoryMovementResponse>> getMovements(@PathVariable Long productId) {
        List<InventoryMovementResponse> response = inventoryMovementService.getMovementsForProduct(productId).stream()
                .map(InventoryMovementResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
