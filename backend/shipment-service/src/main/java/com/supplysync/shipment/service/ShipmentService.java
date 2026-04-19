package com.supplysync.shipment.service;

import com.supplysync.shipment.dto.ShipmentRequest;
import com.supplysync.shipment.dto.ShipmentResponse;
import com.supplysync.shipment.entity.Shipment;
import com.supplysync.shipment.entity.Status;
import com.supplysync.shipment.exception.InvalidRequestException;
import com.supplysync.shipment.exception.InvalidShipmentStateException;
import com.supplysync.shipment.exception.ShipmentNotFoundException;
import com.supplysync.shipment.repository.ShipmentRepository;
import com.supplysync.shipment.service.state.ShipmentState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public ShipmentResponse createShipment(ShipmentRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new InvalidRequestException("Order ID is required");
        }
        if (request.getCarrierName() == null || request.getCarrierName().isBlank()) {
            throw new InvalidRequestException("Carrier is required");
        }

        Shipment shipment = Shipment.builder()
                .orderId(UUID.fromString(request.getOrderId()))
                .carrier(request.getCarrierName())
                .trackingNumber(request.getTrackingNumber())
                .estimatedDelivery(request.getEstimatedDelivery() != null
                        ? LocalDate.parse(request.getEstimatedDelivery()) : null)
                .build();

        shipmentRepository.save(shipment);
        return mapToResponse(shipment);
    }

    public ShipmentResponse getById(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment with id '" + id + "' not found"));
        return mapToResponse(shipment);
    }

    public ShipmentResponse getByOrderId(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment for order '" + orderId + "' not found"));
        return mapToResponse(shipment);
    }

    public Page<ShipmentResponse> getAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return shipmentRepository.findAll(PageRequest.of(page, size, sort))
                .map(this::mapToResponse);
    }

    public ShipmentResponse updateStatus(UUID id, String newStatus) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment with id '" + id + "' not found"));

        Status targetStatus = Status.valueOf(newStatus.toUpperCase());
        ShipmentState currentState = ShipmentState.from(shipment.getStatus());

        if (!currentState.canTransitionTo(targetStatus)) {
            throw new InvalidShipmentStateException(
                    "Cannot transition from " + shipment.getStatus() + " to " + targetStatus);
        }

        shipment.setStatus(targetStatus);
        shipmentRepository.save(shipment);
        return mapToResponse(shipment);
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId().toString())
                .orderId(shipment.getOrderId().toString())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus().name())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}
