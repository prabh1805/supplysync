package com.supplysync.auth.service;

import com.supplysync.auth.client.TenantClient;
import com.supplysync.auth.dto.*;
import com.supplysync.auth.entity.Role;
import com.supplysync.auth.entity.User;
import com.supplysync.auth.exception.InvalidRequestException;
import com.supplysync.auth.exception.UserAlreadyExistsException;
import com.supplysync.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantClient tenantClient;

    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new InvalidRequestException("First name is required");
        }
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            throw new InvalidRequestException("Tenant ID is required");
        }

        userRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException(
                            "User with email '" + request.getEmail() + "' already exists"
                    );
                });

        Role role = Role.VIEWER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            role = Role.valueOf(request.getRole().toUpperCase());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // hash the password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tenantId(UUID.fromString(request.getTenantId()))
                .role(role)
                .build();

        userRepository.save(user);

        // generate JWT token for the newly registered user
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getTenantId().toString()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("Password is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

        // verify password — BCrypt compares the raw password against the stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid email or password");
        }

        // password matches — generate token
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getTenantId().toString()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public OnboardResponse onboard(OnboardRequest request) {
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new InvalidRequestException("Company name is required");
        }
        if (request.getSubdomain() == null || request.getSubdomain().isBlank()) {
            throw new InvalidRequestException("Subdomain is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("Password is required");
        }

        userRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("Email already in use");
                });

        // step 1: create tenant via tenant-service
        TenantClient.TenantResponse tenant = tenantClient.createTenant(
                request.getCompanyName(), request.getSubdomain());

        // step 2: create admin user linked to the new tenant
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tenantId(UUID.fromString(tenant.getId()))
                .role(Role.TENANT_ADMIN)
                .build();

        userRepository.save(user);

        // step 3: generate JWT
        String token = jwtService.generateToken(
                user.getEmail(), user.getRole().name(), user.getTenantId().toString());

        return OnboardResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .build();
    }
}
