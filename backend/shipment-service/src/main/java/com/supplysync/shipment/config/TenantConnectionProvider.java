package com.supplysync.shipment.config;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {
    @Autowired private DataSource dataSource;

    @Override public Connection getAnyConnection() throws SQLException { return dataSource.getConnection(); }
    @Override public void releaseAnyConnection(Connection c) throws SQLException { c.close(); }
    @Override
    public Connection getConnection(String schema) throws SQLException {
        Connection c = dataSource.getConnection();
        c.setSchema(schema);
        return c;
    }
    @Override
    public void releaseConnection(String schema, Connection c) throws SQLException {
        c.setSchema("public");
        c.close();
    }
    @Override public boolean supportsAggressiveRelease() { return false; }
    @Override public boolean isUnwrappableAs(Class<?> t) { return false; }
    @Override public <T> T unwrap(Class<T> t) { return null; }
    @Override
    public void customize(Map<String, Object> p) {
        p.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
