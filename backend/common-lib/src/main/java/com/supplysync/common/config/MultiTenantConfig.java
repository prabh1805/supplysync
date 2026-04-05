package com.supplysync.common.config;

import com.supplysync.common.tenant.TenantConnectionProvider;
import com.supplysync.common.tenant.TenantInterceptor;
import com.supplysync.common.tenant.TenantSchemaResolver;
import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Wires up all multi-tenancy components.
 *
 * Any service that imports this config gets:
 * 1. TenantInterceptor registered on all requests
 * 2. Hibernate configured for schema-based multi-tenancy
 *
 * Usage in a service:
 *   @Import(MultiTenantConfig.class) on the main application class
 */
@Configuration
public class MultiTenantConfig implements WebMvcConfigurer {

    // register the interceptor so it runs on every request
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor());
    }

    // tell Hibernate to use our schema resolver and connection provider
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(DataSource dataSource) {
        return properties -> {
            properties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, new TenantConnectionProvider(dataSource));
            properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, new TenantSchemaResolver());
        };
    }
}
