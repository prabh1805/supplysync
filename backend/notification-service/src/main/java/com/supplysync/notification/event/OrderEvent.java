package com.supplysync.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String tenantId;
    private String type;
    private String status;
    private String supplierId;
    private BigDecimal totalAmount;
}
