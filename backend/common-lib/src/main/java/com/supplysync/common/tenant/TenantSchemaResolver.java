package com.supplysync.common.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Tells Hibernate which tenant (schema) to use for the current request.
 *
 * Hibernate calls this before every DB operation to determine the schema.
 * We just read from TenantContext, which was set by TenantInterceptor.
 *
 * If no tenant is set (e.g., public endpoints), falls back to "public" schema.
 */
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String schema = TenantContext.getSchema();
        return (schema != null) ? schema : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
