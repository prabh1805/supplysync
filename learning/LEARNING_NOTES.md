# SupplySync — Learning Notes

This document tracks every annotation, dependency, configuration, and concept used across the project. It updates as new services and classes are added.

---

## Phase 1: Foundation Services

---

### 1. Discovery Server (Eureka Server)

**What it does:** Acts as a service registry. Every microservice registers itself here so other services can find and talk to each other without hardcoding URLs.

**File: `DiscoveryServerApplication.java`**

```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

**Annotations explained:**

| Annotation | What it does |
|---|---|
| `@SpringBootApplication` | A combo of 3 annotations: `@Configuration` (this class can define beans), `@EnableAutoConfiguration` (Spring Boot auto-configures based on dependencies), `@ComponentScan` (scans this package and sub-packages for Spring components). Every Spring Boot app needs this on the main class. |
| `@EnableEurekaServer` | Turns this Spring Boot app into a Eureka service registry. Without this, it's just a regular app. This annotation activates the Eureka dashboard UI and the REST API that other services use to register/discover. |

**Configuration: `application.properties`**

```properties
spring.application.name=discovery-server
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

| Property | What it does |
|---|---|
| `spring.application.name` | The logical name of this service. Used in logs, Eureka registration, and by other services to reference it. |
| `server.port=8761` | The port this app runs on. 8761 is the conventional Eureka port — other services look here by default. |
| `eureka.client.register-with-eureka=false` | Tells this app NOT to register itself as a client with Eureka. Why? Because it IS the Eureka server — registering with itself would be pointless. |
| `eureka.client.fetch-registry=false` | Tells this app NOT to fetch the service registry from Eureka. Again, it IS the registry — no need to fetch from itself. |

**Dependency:**

```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
```

This pulls in everything needed to run a Eureka server — the dashboard UI, the REST API, and the server-side logic. It also transitively brings in a web server (Tomcat), so you don't need `spring-boot-starter-web` separately.

---

### 2. Config Server (Spring Cloud Config)

**What it does:** Centralizes configuration for all microservices. Instead of each service having its own hardcoded `application.properties`, they pull their config from this server at startup. Change config in one place → all services pick it up.

**File: `ConfigServerApplication.java`**

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**Annotations explained:**

| Annotation | What it does |
|---|---|
| `@SpringBootApplication` | Same as above — the standard entry point annotation. |
| `@EnableConfigServer` | Turns this app into a Spring Cloud Config Server. It exposes REST endpoints like `/{application}/{profile}` that other services call to fetch their configuration. Without this, it's just a regular app. |

**Configuration: `application.properties`**

```properties
spring.application.name=config-server
server.port=8888
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/configurations
```

| Property | What it does |
|---|---|
| `spring.application.name=config-server` | Identifies this service as "config-server" in Eureka and logs. |
| `server.port=8888` | Convention for config servers. When other services use `spring.config.import=configserver:`, they look at port 8888 by default. |
| `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` | Tells this service where the Eureka server is. It will register itself there, so other services can discover it. Unlike discovery-server, this one IS a client — it registers itself. |
| `spring.profiles.active=native` | Config Server supports multiple backends: Git, Vault, JDBC, native (local files). `native` means "serve config from local files." We use this for development simplicity. In production, you'd switch to Git. |
| `spring.cloud.config.server.native.search-locations=classpath:/configurations` | Tells the config server where to find the config files. `classpath:/configurations` means the `configurations/` folder inside `src/main/resources/`. When a service named `discovery-server` asks for config, it looks for `discovery-server.properties` here. |

**Dependencies:**

```groovy
implementation 'org.springframework.cloud:spring-cloud-config-server'
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
```

| Dependency | What it does |
|---|---|
| `spring-cloud-config-server` | The core config server library. Provides the REST endpoints, backend support (Git, native, etc.), and the `@EnableConfigServer` annotation. |
| `spring-cloud-starter-netflix-eureka-client` | Makes this app a Eureka client — it registers itself with the Eureka server so other services can discover it. This is different from `eureka-server` which we used in discovery-server. |

**Config file served: `configurations/discovery-server.properties`**

This file lives inside the config server's resources. When the discovery-server (or any service named "discovery-server") asks the config server for its config, this file is what gets served back. The filename must match the `spring.application.name` of the requesting service.

---

## How the Services Connect (so far)

```
┌─────────────────────┐
│   Discovery Server  │  ← Port 8761, the registry
│   (Eureka Server)   │
└─────────┬───────────┘
          │ registers
┌─────────▼───────────┐
│    Config Server    │  ← Port 8888, serves config files
│  (Eureka Client)    │
└─────────────────────┘
```

1. Discovery Server starts first — it's the registry, doesn't register itself
2. Config Server starts — registers itself with Eureka at `localhost:8761/eureka/`
3. Config Server loads config files from `src/main/resources/configurations/`
4. Later, when other services start, they'll ask Config Server for their config

---

## Key Concepts

### Service Registry (Eureka)
In a microservices architecture, services need to find each other. Instead of hardcoding URLs (`http://localhost:8081`), services register with a registry and look each other up by name. Eureka is Netflix's implementation of this pattern. It handles registration, heartbeats (checking if services are alive), and deregistration.

### Centralized Configuration
When you have 10+ services, managing `application.properties` in each one becomes a nightmare. Config Server solves this — one place to manage all configs. Services pull their config at startup. You can even refresh config at runtime without restarting services (using Spring Cloud Bus — we'll add this later).

### Spring Cloud BOM (Bill of Materials)
```groovy
ext {
    set('springCloudVersion', "2025.1.1")
}
dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```
This ensures all Spring Cloud libraries use compatible versions. Without it, you'd have to manually specify versions for each Spring Cloud dependency and risk version conflicts. The BOM says "for Spring Cloud 2025.1.1, use these exact versions of each library."

---

*This document will be updated as new services and classes are added.*
