package com.supplysync.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private String id;
    private String orderId;
    private String carrier;
    private String trackingNumber;
    private String status;
    private LocalDate estimatedDelivery;
    private LocalDateTime createdAt;
}
