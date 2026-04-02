package com.supplysync.tenant.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TenantResponse {
    private UUID id;
    private String name;
    private String subdomain;
    private String dbSchema;
    private String plan;
    private String status;
}
