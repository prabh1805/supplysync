package com.supplysync.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    @Builder.Default
    private String token = "";
    private String email;
    private String fullName;
    private String role;
}
