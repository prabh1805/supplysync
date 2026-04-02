package com.supplysync.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private UUID id;
    private String name;
    private String subdomain;
    private String dbSchema;
    private String plan;
    private String status;
}
