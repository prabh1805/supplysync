package com.supplysync.tenant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantRequest {
    private String name;       // company name — required
    private String subdomain;  // unique identifier like "acme" — required
    private String plan;       // optional — defaults to FREE if not provided
    // No status here — status is always ACTIVE on creation
    // No dbSchema here — auto-generated from subdomain ("tenant_acme")
}
