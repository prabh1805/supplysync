package com.supplysync.shipment.controller;

import com.supplysync.shipment.dto.ShipmentRequest;
import com.supplysync.shipment.dto.ShipmentResponse;
import com.supplysync.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@RequestBody ShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.createShipment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.getByOrderId(orderId));
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(shipmentService.getAll(page, size, sortBy, direction));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<ShipmentResponse> updateStatus(@PathVariable UUID id, @PathVariable String status) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, status));
    }
}
