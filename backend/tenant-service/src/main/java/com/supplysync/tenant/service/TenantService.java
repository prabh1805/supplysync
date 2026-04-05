package com.supplysync.tenant.service;

import com.supplysync.tenant.dto.TenantRequest;
import com.supplysync.tenant.dto.TenantResponse;
import com.supplysync.tenant.entity.Plan;
import com.supplysync.tenant.entity.Status;
import com.supplysync.tenant.entity.Tenant;
import com.supplysync.tenant.exception.InvalidRequestException;
import com.supplysync.tenant.exception.TenantAlreadyExistsException;
import com.supplysync.tenant.exception.TenantNotFoundException;
import com.supplysync.tenant.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public TenantResponse createTenant(TenantRequest tenantRequest) {
        if (tenantRequest.getName() == null || tenantRequest.getName().isBlank()) {
            throw new InvalidRequestException("Tenant name is required");
        }
        if (tenantRequest.getSubdomain() == null || tenantRequest.getSubdomain().isBlank()) {
            throw new InvalidRequestException("Subdomain is required");
        }

        tenantRepository.findBySubdomain(tenantRequest.getSubdomain())
                .ifPresent(existing -> {
                    throw new TenantAlreadyExistsException(
                            "Tenant with subdomain '" + tenantRequest.getSubdomain() + "' already exists"
                    );
                });

        Plan plan = Plan.FREE;
        if (tenantRequest.getPlan() != null && !tenantRequest.getPlan().isBlank()) {
            plan = Plan.valueOf(tenantRequest.getPlan().toUpperCase());
        }

        String schemaName = "tenant_" + tenantRequest.getSubdomain().toLowerCase();

        Tenant tenant = Tenant.builder()
                .name(tenantRequest.getName())
                .subdomain(tenantRequest.getSubdomain())
                .dbSchema(schemaName)
                .plan(plan)
                .build();

        tenantRepository.save(tenant);

        // create the tenant's schema in PostgreSQL
        // this is where the isolation happens — each tenant gets their own schema
        createTenantSchema(schemaName);

        return mapToResponse(tenant);
    }

    public TenantResponse getTenantById(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant with id '" + tenantId + "' not found"
                ));
        return mapToResponse(tenant);
    }

    public TenantResponse getTenantBySubDomain(String subdomain) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant with subdomain '" + subdomain + "' not found"
                ));
        return mapToResponse(tenant);
    }

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TenantResponse updateTenantStatus(UUID tenantId, String status) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant with id '" + tenantId + "' not found"
                ));

        Status newStatus = Status.valueOf(status.toUpperCase());
        tenant.setStatus(newStatus);
        tenantRepository.save(tenant);
        return mapToResponse(tenant);
    }

    /**
     * Creates a new PostgreSQL schema for the tenant and sets up the initial tables.
     * Each tenant gets isolated tables (products, orders, inventory, etc.)
     */
    private void createTenantSchema(String schemaName) {
        // create the schema
        entityManager.createNativeQuery("CREATE SCHEMA IF NOT EXISTS " + schemaName).executeUpdate();

        // create the products table in the new schema
        // more tables will be added as we build more services
        entityManager.createNativeQuery(
                "CREATE TABLE IF NOT EXISTS " + schemaName + ".products (" +
                "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "name VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "sku VARCHAR(100) UNIQUE NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, " +
                "category VARCHAR(100), " +
                "active BOOLEAN DEFAULT true, " +
                "created_at TIMESTAMP NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMP" +
                ")"
        ).executeUpdate();
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .dbSchema(tenant.getDbSchema())
                .plan(tenant.getPlan().name())
                .status(tenant.getStatus().name())
                .build();
    }
}
