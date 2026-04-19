package com.supplysync.auth.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TenantClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8081/api/v1/tenants")
            .build();

    public TenantResponse createTenant(String name, String subdomain) {
        return restClient.post()
                .body(Map.of("name", name, "subdomain", subdomain))
                .retrieve()
                .body(TenantResponse.class);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantResponse {
        private String id;
        private String name;
        private String subdomain;
        private String dbSchema;
        private String plan;
        private String status;
    }
}
