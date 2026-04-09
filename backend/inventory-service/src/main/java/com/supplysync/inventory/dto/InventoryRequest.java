package com.supplysync.inventory.dto;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    private String productId;
    private String  warehouse;
    private int quantity;
    private int minStockLevel;
}
