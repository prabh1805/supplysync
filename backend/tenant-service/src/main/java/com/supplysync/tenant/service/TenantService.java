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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantResponse createTenant(TenantRequest tenantRequest) {
        // Validate required fields
        if (tenantRequest.getName() == null || tenantRequest.getName().isBlank()) {
            throw new InvalidRequestException("Tenant name is required");
        }
        if (tenantRequest.getSubdomain() == null || tenantRequest.getSubdomain().isBlank()) {
            throw new InvalidRequestException("Subdomain is required");
        }

        // Check for duplicates
        tenantRepository.findBySubdomain(tenantRequest.getSubdomain())
                .ifPresent(existing -> {
                    throw new TenantAlreadyExistsException(
                            "Tenant with subdomain '" + tenantRequest.getSubdomain() + "' already exists"
                    );
                });

        // Parse plan — defaults to FREE if not provided
        Plan plan = Plan.FREE;
        if (tenantRequest.getPlan() != null && !tenantRequest.getPlan().isBlank()) {
            plan = Plan.valueOf(tenantRequest.getPlan().toUpperCase());
            // ↑ If someone sends "GOLD", this throws IllegalArgumentException
            //   which our GlobalExceptionHandler catches and returns 400
        }

        Tenant tenant = Tenant.builder()
                .name(tenantRequest.getName())
                .subdomain(tenantRequest.getSubdomain())
                .dbSchema("tenant_" + tenantRequest.getSubdomain().toLowerCase())
                .plan(plan)
                .build();

        tenantRepository.save(tenant);
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
        // ↑ Cleaner than creating an ArrayList and forEach-ing.
        //   .stream() converts the list to a stream
        //   .map(this::mapToResponse) transforms each Tenant → TenantResponse
        //   .toList() collects back into an immutable list
    }

    public TenantResponse updateTenantStatus(UUID tenantId, String status) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant with id '" + tenantId + "' not found"
                ));

        // valueOf will throw IllegalArgumentException if status is invalid
        // GlobalExceptionHandler catches that → returns 400
        Status newStatus = Status.valueOf(status.toUpperCase());
        tenant.setStatus(newStatus);
        tenantRepository.save(tenant);
        return mapToResponse(tenant);
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
