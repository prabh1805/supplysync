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

### 3. API Gateway (Spring Cloud Gateway)

**What it does:** Single entry point for all client requests. Routes requests to the correct microservice, handles cross-cutting concerns like rate limiting, authentication forwarding, and CORS.

**File: `ApiGatewayApplication.java`**

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

No special annotation needed — the Gateway starter auto-configures everything.

**Configuration: `application.properties`**

```properties
spring.application.name=api-gateway
server.port=8080
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
```

| Property | What it does |
|---|---|
| `spring.cloud.gateway.discovery.locator.enabled=true` | Auto-creates routes for every service in Eureka. If `tenant-service` is registered, you can hit `http://localhost:8080/tenant-service/api/v1/tenants` and it routes automatically. |
| `spring.cloud.gateway.discovery.locator.lower-case-service-id=true` | Eureka registers services in UPPERCASE. This lets you use lowercase in URLs. |

**Dependency:**

```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc'
```

This is the servlet-based (MVC) variant of Spring Cloud Gateway. There's also a reactive variant (`spring-cloud-starter-gateway`). MVC variant runs on Tomcat, reactive runs on Netty.

---

## Phase 2: Tenant Service

---

### 4. Tenant Service

**What it does:** Manages tenant (company) onboarding, configuration, and lifecycle. Every other service depends on tenant context. This is the backbone of multi-tenancy.

---

#### Entity: `Tenant.java`

```java
@Entity
@Table(name="tenants")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    private String dbSchema;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Plan plan = Plan.FREE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Annotations explained:**

| Annotation | What it does |
|---|---|
| `@Entity` | Marks this class as a JPA entity — Hibernate will map it to a database table. |
| `@Table(name="tenants")` | Specifies the table name. Without it, Hibernate uses the class name. |
| `@EntityListeners(AuditingEntityListener.class)` | Registers the JPA auditing listener on this entity. Required for `@CreatedDate` and `@LastModifiedDate` to work. Without this, those fields stay null. |
| `@Id` | Marks the primary key field. |
| `@GeneratedValue(strategy = GenerationType.UUID)` | Auto-generates a UUID for the primary key on insert. |
| `@Column(nullable = false, unique = true)` | Database constraints — NOT NULL and UNIQUE. Enforced at the DB level. |
| `@Enumerated(EnumType.STRING)` | Stores the enum as its string name (e.g., "FREE") instead of ordinal (0, 1, 2). Always use STRING — ordinals break if you reorder enum values. |
| `@Builder.Default` | When using `@Builder`, fields with default values need this annotation, otherwise the builder ignores the default and sets null. |
| `@CreatedDate` | JPA auditing — automatically sets this field to the current timestamp when the entity is first persisted. Requires `@EnableJpaAuditing` on the main class AND `@EntityListeners` on the entity. |
| `@LastModifiedDate` | JPA auditing — automatically updates this field to the current timestamp on every save/update. |
| `@EnableJpaAuditing` | Goes on the main application class. Enables Spring Data JPA's auditing infrastructure globally. Without this, `@CreatedDate` and `@LastModifiedDate` do nothing. |

**Lombok annotations on entities:**

| Annotation | What it does |
|---|---|
| `@Getter` / `@Setter` | Generates getters and setters for all fields. |
| `@Builder` | Generates a builder pattern — `Tenant.builder().name("Acme").build()`. Cleaner than telescoping constructors. |
| `@NoArgsConstructor` | Generates a no-args constructor. Required by JPA — Hibernate needs it to instantiate entities via reflection. |
| `@AllArgsConstructor` | Generates an all-args constructor. Required by `@Builder` when `@NoArgsConstructor` is also present. |

---

#### DTOs: `TenantRequest.java` and `TenantResponse.java`

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantRequest {
    private String name;
    private String subdomain;
    private String plan;
}
```

**Why DTOs?** Never expose your entity directly to the API. Reasons:
1. Security — entities may have fields you don't want clients to see or set
2. Decoupling — your API contract shouldn't change when your DB schema changes
3. Validation — DTOs can have different validation rules than entities

**Why `@NoArgsConstructor` + `@AllArgsConstructor` on DTOs?**
Jackson (the JSON serializer) needs a no-args constructor to create an empty object, then uses setters to fill fields from JSON. `@Builder` generates only a private all-args constructor. Without `@NoArgsConstructor`, Jackson throws: `Type definition error: [simple type, class TenantRequest]`.

Rule: On any DTO that receives JSON via `@RequestBody`, always add `@NoArgsConstructor` + `@AllArgsConstructor` alongside `@Builder`.

---

#### Repository: `TenantRepository.java`

```java
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySubdomain(String subdomain);
}
```

| Concept | Explanation |
|---|---|
| `JpaRepository<Tenant, UUID>` | Provides CRUD operations for free: `save()`, `findById()`, `findAll()`, `delete()`, etc. The generics are `<EntityType, PrimaryKeyType>`. |
| `findBySubdomain(String)` | Spring Data query derivation — Spring reads the method name and generates the SQL: `SELECT * FROM tenants WHERE subdomain = ?`. No implementation needed. |
| `Optional<Tenant>` | Returns an Optional because the tenant might not exist. Forces you to handle the "not found" case explicitly. |

---

#### Service: `TenantService.java`

Key patterns used:
- **Builder pattern** — constructing `Tenant` and `TenantResponse` objects
- **Method reference** — `this::mapToResponse` in stream operations
- **Stream API** — `.stream().map().toList()` instead of manual loops

```java
// Stream API example — cleaner than ArrayList + forEach
return tenantRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .toList();
```

---

#### Controller: `TenantController.java`

**Annotations explained:**

| Annotation | What it does |
|---|---|
| `@RestController` | Combo of `@Controller` + `@ResponseBody`. Every method return value is serialized to JSON automatically. |
| `@RequestMapping("/api/v1/tenants")` | Base URL path for all endpoints in this controller. |
| `@PostMapping` | Maps HTTP POST requests. `POST /api/v1/tenants` = create a tenant. |
| `@GetMapping("/{id}")` | Maps HTTP GET with a path variable. `{id}` is extracted into the method parameter. |
| `@PatchMapping` | Maps HTTP PATCH — for partial updates (e.g., just updating status). |
| `@PathVariable` | Extracts a value from the URL path. `/{id}` → `@PathVariable UUID id`. |
| `@RequestBody` | Deserializes the JSON request body into the parameter object. |
| `@RequiredArgsConstructor` | Lombok — generates a constructor for all `final` fields. Spring uses this for dependency injection (constructor injection). |

**ResponseEntity and HTTP Status Codes:**

```java
// 201 Created — for POST (new resource created)
return ResponseEntity.status(HttpStatus.CREATED).body(res);

// 200 OK — for GET (returning existing data)
return ResponseEntity.ok(res);
```

| Status Code | When to use |
|---|---|
| 200 OK | Successful GET, PATCH, PUT |
| 201 Created | Successful POST (new resource) |
| 400 Bad Request | Invalid input from client |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | Duplicate resource (e.g., subdomain already taken) |
| 500 Internal Server Error | Unexpected server failure |

---

#### Custom Exception Handling

**Exception classes:** Simple classes extending `RuntimeException`:
- `TenantNotFoundException` → thrown when tenant not found by ID or subdomain
- `TenantAlreadyExistsException` → thrown when creating a tenant with duplicate subdomain
- `InvalidRequestException` → thrown for bad input (null fields, etc.)

**`GlobalExceptionHandler.java` — the key piece:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTenantNotFound(TenantNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    // ... more handlers
}
```

| Concept | Explanation |
|---|---|
| `@RestControllerAdvice` | `@ControllerAdvice` + `@ResponseBody`. Intercepts exceptions from ALL controllers. Spring looks for matching `@ExceptionHandler` methods here before returning errors to the client. |
| `@ExceptionHandler(SomeException.class)` | Tells Spring "when this exception type is thrown, call this method." Spring matches the most specific exception first. |
| `ErrorResponse` | A DTO for consistent error JSON. Every error returns the same structure: status, error label, message, timestamp. |
| Generic `Exception` handler | Safety net — catches anything not explicitly handled. Returns 500 with a generic message. Never leak internal details to clients. |

**The flow:**
```
Controller → Service throws TenantNotFoundException
  → Spring catches it
  → Finds @ExceptionHandler(TenantNotFoundException.class) in GlobalExceptionHandler
  → Builds ErrorResponse with 404
  → Returns JSON to client
```

---

## Architecture So Far

```
┌─────────────────────┐
│   Discovery Server  │  ← Port 8761
│   (Eureka Server)   │
└─────────┬───────────┘
          │ registers
┌─────────▼───────────┐
│    Config Server    │  ← Port 8888
│  (Eureka Client)    │
└─────────┬───────────┘
          │ registers
┌─────────▼───────────┐
│    API Gateway      │  ← Port 8080
│  (Eureka Client)    │
└─────────┬───────────┘
          │ routes to
┌─────────▼───────────┐
│   Tenant Service    │  ← Port 8081
│  (Eureka Client)    │  ← PostgreSQL: supplysync_tenants
└─────────────────────┘
```

---

## Lessons Learned

1. **Lombok `@Builder` + Jackson**: DTOs that receive JSON via `@RequestBody` need `@NoArgsConstructor` + `@AllArgsConstructor`. Without them, Jackson can't deserialize the JSON.

2. **JPA Auditing requires two things**: `@EnableJpaAuditing` on the main class AND `@EntityListeners(AuditingEntityListener.class)` on each entity using `@CreatedDate`/`@LastModifiedDate`.

3. **Route conflicts**: `@GetMapping("/{id}")` and `@GetMapping("/{subdomain}")` conflict — Spring can't distinguish them. Use `/subdomain/{subdomain}` for the second one.

4. **REST conventions**: POST returns 201, not 200. Use `ResponseEntity.status(HttpStatus.CREATED).body(res)`.

5. **Eureka is non-blocking**: If the discovery server is down, services still start — Eureka registration fails silently and retries in the background.

---

*This document will be updated as new services and classes are added.*
