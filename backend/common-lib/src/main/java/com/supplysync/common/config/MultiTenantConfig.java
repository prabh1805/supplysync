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

@Configuration
public class MultiTenantConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor());
    }

    @Bean
    public TenantSchemaResolver tenantSchemaResolver() {
        return new TenantSchemaResolver();
    }

    @Bean
    public TenantConnectionProvider tenantConnectionProvider(DataSource dataSource) {
        return new TenantConnectionProvider(dataSource);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernateMultiTenancyCustomizer(
            TenantConnectionProvider connectionProvider,
            TenantSchemaResolver schemaResolver) {
        return properties -> {
            properties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
            properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, schemaResolver);
        };
    }
}
