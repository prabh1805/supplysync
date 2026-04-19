package com.supplysync.shipment.repository;

import com.supplysync.shipment.entity.Shipment;
import com.supplysync.shipment.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);
    List<Shipment> findByStatus(Status status);
}
