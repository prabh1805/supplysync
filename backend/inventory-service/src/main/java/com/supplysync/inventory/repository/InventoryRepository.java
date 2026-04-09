package com.supplysync.inventory.repository;

import com.supplysync.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {
    Optional<InventoryItem> findByProductId(UUID productId);
    Optional<InventoryItem> findByWarehouse(String warehouse);
    Optional<InventoryItem> findByProductIdAndWarehouse(UUID productId, String warehouse);
}
