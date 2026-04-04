package com.supplysync.auth.config;

import com.supplysync.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * This filter runs ONCE for every HTTP request (OncePerRequestFilter).
 *
 * Flow:
 * 1. Check if Authorization header exists and starts with "Bearer "
 * 2. If no token → skip, let the request continue (public endpoints will work)
 * 3. If token exists → validate it
 * 4. If valid → extract user info, set SecurityContext (Spring now knows who you are)
 * 5. If invalid → skip, Spring Security will reject with 401
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. no token? let it through (public endpoints don't need one)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 4. validate
        if (jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            // 5. create an Authentication object and set it in SecurityContext
            // this tells Spring Security "this user is authenticated with this role"
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email,                                          // principal (who)
                            null,                                           // credentials (not needed, token already validated)
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))  // authorities (roles)
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 6. continue the filter chain
        filterChain.doFilter(request, response);
    }
}
