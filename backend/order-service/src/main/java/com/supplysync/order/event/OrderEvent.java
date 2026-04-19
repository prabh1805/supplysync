package com.supplysync.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event published to Kafka when an order's status changes.
 * Other services (inventory, notification) consume this.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String tenantId;
    private String type;       // PURCHASE or SALE
    private String status;     // the new status
    private String supplierId;
    private BigDecimal totalAmount;
}
