package com.supplysync.supplier.repository;

import com.supplysync.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByEmail(String email);
    Page<Supplier> findByNameContainingIgnoreCase(String name, PageRequest pageRequest);
}
