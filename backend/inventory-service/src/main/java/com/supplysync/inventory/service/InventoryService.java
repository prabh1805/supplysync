package com.supplysync.inventory.service;

import com.supplysync.inventory.dto.InventoryRequest;
import com.supplysync.inventory.dto.InventoryResponse;
import com.supplysync.inventory.entity.InventoryItem;
import com.supplysync.inventory.exception.InsufficientStockException;
import com.supplysync.inventory.exception.InventoryNotFoundException;
import com.supplysync.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryResponse createAndUpdateInventory(InventoryRequest inventoryRequest) {
        if(inventoryRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity should be greater than 0");
        }
        if(inventoryRequest.getProductId() == null) {
            throw new IllegalArgumentException("Product id should not be null");
        }

        if(inventoryRequest.getWarehouse() == null) {
            throw new IllegalArgumentException("Warehouse should not be null");
        }

        if(inventoryRequest.getMinStockLevel() <= 0) {
            throw new IllegalArgumentException("Min stock level should be greater than 0");
        }

        InventoryItem inventoryItem = inventoryRepository
                .findByProductIdAndWarehouse(UUID.fromString(inventoryRequest.getProductId()),
                        inventoryRequest.getWarehouse()
                ).map(inventoryItem1 -> {
                    inventoryItem1.setQuantity(inventoryRequest.getQuantity());
                    return inventoryItem1;
                }).orElseGet(() -> createInventory(inventoryRequest));

        inventoryRepository.saveAndFlush(inventoryItem);
        return mapToResponse(inventoryItem);
    }

    public InventoryResponse getStockByProductId(String productId) {
        UUID uuid = UUID.fromString(productId);
        return inventoryRepository.findByProductId(uuid)
                .map(this::mapToResponse)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));
    }

    public InventoryResponse getStockByProductAndWarehouse(String productId, String warehouse) {
        UUID uuid = UUID.fromString(productId);
        return inventoryRepository.findByProductIdAndWarehouse(uuid, warehouse)
                .map(this::mapToResponse)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));

    }

    public Page<InventoryResponse> getAllStock(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return inventoryRepository.findAll(PageRequest.of(page, size, sort))
                .map(this::mapToResponse);
    }

    public List<InventoryResponse> getLowStock() {
        return inventoryRepository.findAll()
                .stream()
                .filter(item -> item.getQuantity() <= item.getMinStockLevel())
                .map(this::mapToResponse)
                .toList();
    }

    public InventoryResponse deductStock(String productId, InventoryRequest inventoryRequest) {
        if(inventoryRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity should be greater than 0");
        }
        if(inventoryRequest.getProductId() == null) {
            throw new IllegalArgumentException("Product id should not be null");
        }
        if(inventoryRequest.getWarehouse() == null) {
            throw new IllegalArgumentException("Warehouse should not be null");
        }
        UUID uuid = UUID.fromString(productId);
        InventoryItem item = inventoryRepository.findByProductIdAndWarehouse(uuid, inventoryRequest.getWarehouse())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));
        if(item.getQuantity() < inventoryRequest.getQuantity()) {
            throw new InsufficientStockException("Quantity should be greater than 0");
        }
        item.setQuantity(item.getQuantity() - inventoryRequest.getQuantity());
        inventoryRepository.saveAndFlush(item);
        return mapToResponse(item);
    }

    private InventoryItem createInventory(InventoryRequest inventoryRequest){
        return InventoryItem.builder()
                .productId(UUID.fromString(inventoryRequest.getProductId()))
                .warehouse(inventoryRequest.getWarehouse())
                .quantity(inventoryRequest.getQuantity())
                .minStockLevel(inventoryRequest.getMinStockLevel())
                .build();
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId().toString())
                .productId(item.getProductId().toString())
                .quantity(item.getQuantity())
                .warehouse(item.getWarehouse())
                .minStockLevel(item.getMinStockLevel())
                .lowStock(item.getQuantity() <= item.getMinStockLevel())
                .lastUpdated(item.getUpdatedAt())
                .build();
    }
}
