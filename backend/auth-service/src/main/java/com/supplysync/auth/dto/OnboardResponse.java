package com.supplysync.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private String tenantId;
    private String tenantName;
    private String subdomain;
}
