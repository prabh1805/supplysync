package com.supplysync.common.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts every HTTP request and extracts the tenant schema.
 *
 * Expects header: X-Tenant-ID: tenant_acme
 * The value should be the schema name (e.g., "tenant_acme"), not the subdomain.
 *
 * In production, you'd resolve the subdomain to a schema name via a lookup.
 * For now, the client (or API gateway) sends the schema name directly.
 */
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantSchema = request.getHeader(TENANT_HEADER);

        if (tenantSchema != null && !tenantSchema.isBlank()) {
            TenantContext.setSchema(tenantSchema);
        }

        return true; // continue processing the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // always clear after request completes to prevent leaking tenant context
        // to the next request that reuses this thread
        TenantContext.clear();
    }
}
