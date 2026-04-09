package com.supplysync.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String id;
    private String productId;
    private String warehouse;
    private int quantity;
    private int minStockLevel;
    private boolean lowStock;
    private LocalDateTime lastUpdated;
}
