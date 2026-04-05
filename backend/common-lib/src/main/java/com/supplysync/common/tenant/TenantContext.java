package com.supplysync.common.tenant;

/**
 * Holds the current tenant's schema name for the duration of a request.
 *
 * ThreadLocal = each thread gets its own copy of the variable.
 * Since each HTTP request runs on its own thread in Tomcat,
 * this effectively gives us per-request tenant storage.
 *
 * Flow:
 *   TenantInterceptor sets it → Hibernate reads it → request ends → cleared
 */
public class TenantContext {

    private static final ThreadLocal<String> currentSchema = new ThreadLocal<>();

    public static void setSchema(String schema) {
        currentSchema.set(schema);
    }

    public static String getSchema() {
        return currentSchema.get();
    }

    // must be called after every request to prevent memory leaks
    public static void clear() {
        currentSchema.remove();
    }
}
