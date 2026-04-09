package com.supplysync.inventory.controller;

import com.supplysync.inventory.dto.InventoryRequest;
import com.supplysync.inventory.dto.InventoryResponse;
import com.supplysync.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> addStock(@RequestBody InventoryRequest request) {
        InventoryResponse res = inventoryService.createAndUpdateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getStockByProduct(@PathVariable UUID productId) {
        InventoryResponse res = inventoryService.getStockByProductId(productId.toString());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/product/{productId}/warehouse/{warehouse}")
    public ResponseEntity<InventoryResponse> getStockByProductAndWarehouse(
            @PathVariable UUID productId,
            @PathVariable String warehouse) {
        InventoryResponse res = inventoryService.getStockByProductAndWarehouse(productId.toString(), warehouse);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> getAllStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "warehouse") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Page<InventoryResponse> res = inventoryService.getAllStock(page, size, sortBy, direction);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStock() {
        List<InventoryResponse> res = inventoryService.getLowStock();
        return ResponseEntity.ok(res);
    }

    @PostMapping("/deduct")
    public ResponseEntity<InventoryResponse> deductStock(@RequestBody InventoryRequest request) {
        InventoryResponse res = inventoryService.deductStock(request.getProductId(), request);
        return ResponseEntity.ok(res);
    }
}
