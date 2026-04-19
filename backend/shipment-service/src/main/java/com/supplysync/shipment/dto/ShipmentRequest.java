package com.supplysync.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    private String orderId;
    private String  carrierName;
    private String  trackingNumber;
    private String estimatedDelivery;
}
