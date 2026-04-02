package com.supplysync.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;        // HTTP status code (e.g., 404, 409, 400)
    private String error;      // Short label (e.g., "Not Found", "Conflict")
    private String message;    // Actual error detail (e.g., "Tenant with subdomain 'acme' not found")
    private LocalDateTime timestamp;
}
