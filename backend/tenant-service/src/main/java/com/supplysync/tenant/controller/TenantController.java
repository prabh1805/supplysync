package com.supplysync.tenant.controller;

import com.supplysync.tenant.dto.TenantRequest;
import com.supplysync.tenant.dto.TenantResponse;
import com.supplysync.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    // POST /api/v1/tenants → create a new tenant
    // Returns 201 Created (not 200 OK) because a new resource was created
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@RequestBody TenantRequest request) {
        TenantResponse res = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
        // HttpStatus.CREATED = 201
        // Why not .ok()? Because .ok() returns 200, which means "here's what you asked for."
        // 201 means "I created something new for you." Semantically correct REST.
    }

    // GET /api/v1/tenants/{id} → get tenant by UUID
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
        TenantResponse res = tenantService.getTenantById(id);
        return ResponseEntity.ok(res);
        // .ok() = 200 — correct here because we're just fetching, not creating
    }

    // GET /api/v1/tenants/subdomain/{subdomain} → get tenant by subdomain
    // Note: this is /subdomain/{subdomain}, NOT /{subdomain}
    // Because /{subdomain} would conflict with /{id} — Spring can't tell them apart
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<TenantResponse> getTenantBySubdomain(@PathVariable String subdomain) {
        TenantResponse res = tenantService.getTenantBySubDomain(subdomain);
        return ResponseEntity.ok(res);
    }

    // GET /api/v1/tenants → get all tenants
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getTenants() {
        List<TenantResponse> res = tenantService.getAllTenants();
        return ResponseEntity.ok(res);
    }

    // PATCH /api/v1/tenants/{id}/status/{status} → update tenant status
    // PATCH (not PUT) because we're partially updating the resource (just the status field)
    // PUT would mean "replace the entire resource"
    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<TenantResponse> updateTenantStatus(
            @PathVariable UUID id,
            @PathVariable String status) {
        TenantResponse res = tenantService.updateTenantStatus(id, status);
        return ResponseEntity.ok(res);
    }
}
