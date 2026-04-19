package com.supplysync.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardRequest {
    private String companyName;
    private String subdomain;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
