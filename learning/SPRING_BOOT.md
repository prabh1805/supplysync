# Spring Boot — Complete Interview Guide

---

## 1. Spring Core — IoC & Dependency Injection

**Inversion of Control (IoC):** Spring container manages object creation and lifecycle, not you.

**Dependency Injection Types:**
```java
// 1. Constructor Injection (RECOMMENDED)
@Service
public class OrderService {
    private final OrderRepository repo;

    public OrderService(OrderRepository repo) { // @Autowired optional if single constructor
        this.repo = repo;
    }
}

// 2. Setter Injection
@Autowired
public void setRepo(OrderRepository repo) { this.repo = repo; }

// 3. Field Injection (avoid — hard to test)
@Autowired
private OrderRepository repo;
```

**Why Constructor Injection?** Immutability (final fields), mandatory dependencies enforced, easier unit testing.

**Bean Scopes:**
| Scope | Description |
|-------|-------------|
| singleton | One instance per Spring container (DEFAULT) |
| prototype | New instance every time requested |
| request | One per HTTP request (web only) |
| session | One per HTTP session (web only) |
| application | One per ServletContext |

**Bean Lifecycle:** Constructor → @PostConstruct → afterPropertiesSet() → Custom init → Ready → @PreDestroy → destroy()

**Stereotype Annotations:**
- `@Component` — generic Spring bean
- `@Service` — business logic layer
- `@Repository` — data access layer (adds exception translation)
- `@Controller` / `@RestController` — web layer
- `@Configuration` + `@Bean` — manual bean definition

---

## 2. Spring Boot Auto-Configuration

**How it works:**
1. `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
2. Spring Boot reads `META-INF/spring.factories` / `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
3. Conditional annotations decide what to configure:
   - `@ConditionalOnClass` — class exists on classpath
   - `@ConditionalOnMissingBean` — no user-defined bean exists
   - `@ConditionalOnProperty` — property is set

**Properties:**
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

```java
// Type-safe configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiration;
    // getters, setters
}
```

**Profiles:** `spring.profiles.active=dev` → loads `application-dev.yml`

---

## 3. Spring MVC / REST

**Controller:**
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping
    public ResponseEntity<List<Order>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(orders.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.create(request);
        URI location = URI.create("/api/v1/orders/" + order.getId());
        return ResponseEntity.created(location).body(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Long id,
                                         @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Validation:**
```java
public class CreateOrderRequest {
    @NotBlank(message = "Product name is required")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Email
    private String customerEmail;
}
```

**Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, message));
    }
}
```

---

## 4. Spring Data JPA

**Entity:**
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

**Repository:**
```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Derived query
    List<Order> findByStatusAndCustomerId(OrderStatus status, Long customerId);

    // Custom JPQL
    @Query("SELECT o FROM Order o WHERE o.createdAt > :date AND o.status = :status")
    List<Order> findRecentByStatus(@Param("date") LocalDateTime date,
                                    @Param("status") OrderStatus status);

    // Native query
    @Query(value = "SELECT * FROM orders WHERE total > ?1", nativeQuery = true)
    List<Order> findExpensiveOrders(double minTotal);

    // Pagination
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
}
```

**Key Concepts:**
- Lazy vs Eager loading (N+1 problem — use `@EntityGraph` or `JOIN FETCH`)
- `@Transactional` — method-level transaction management, rollback on unchecked exceptions
- `@Transactional` propagation: REQUIRED (default), REQUIRES_NEW, NESTED, SUPPORTS
- Auditing — `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`
- Specifications — dynamic queries for complex filtering

---

## 5. Spring Security

**Security Filter Chain:**
```
Request → SecurityFilterChain → Authentication → Authorization → Controller
```

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**JWT Flow:**
1. User sends credentials → `/api/auth/login`
2. Server validates → generates JWT (header.payload.signature)
3. Client stores token → sends in `Authorization: Bearer <token>` header
4. JwtAuthFilter extracts token → validates → sets SecurityContext
5. Request proceeds to controller with authenticated user

**Key Concepts:** UserDetailsService, AuthenticationProvider, GrantedAuthority, @PreAuthorize, @Secured, CORS configuration

---

## 6. Microservices with Spring Boot

**Service Discovery (Eureka):**
```yaml
# Eureka Server
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

# Eureka Client (each microservice)
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**API Gateway (Spring Cloud Gateway):**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
```

**Inter-Service Communication:**
- **Synchronous:** RestTemplate (legacy), WebClient (reactive), OpenFeign (declarative)
- **Asynchronous:** RabbitMQ, Apache Kafka

**OpenFeign:**
```java
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory/{productId}")
    InventoryResponse checkStock(@PathVariable String productId);
}
```

**Resilience (Circuit Breaker):**
```java
@CircuitBreaker(name = "inventory", fallbackMethod = "fallback")
public InventoryResponse checkStock(String productId) {
    return inventoryClient.checkStock(productId);
}

public InventoryResponse fallback(String productId, Throwable t) {
    return new InventoryResponse(productId, 0, "UNKNOWN");
}
```

**Key Patterns:** Circuit Breaker, Saga, API Gateway, Service Registry, Config Server, Distributed Tracing

---

## 7. Testing

```java
// Unit test with Mockito
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepo;
    @InjectMocks private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        when(orderRepo.save(any())).thenReturn(testOrder);
        Order result = orderService.create(request);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepo).save(any());
    }
}

// Integration test
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {
    @Autowired private MockMvc mockMvc;

    @Test
    void shouldReturnOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }
}

// Repository test
@DataJpaTest
class OrderRepositoryTest {
    @Autowired private OrderRepository repo;
    @Autowired private TestEntityManager em;

    @Test
    void shouldFindByStatus() {
        em.persist(new Order("Product1", OrderStatus.PENDING));
        List<Order> result = repo.findByStatus(OrderStatus.PENDING);
        assertThat(result).hasSize(1);
    }
}
```

**Test Annotations Cheat Sheet:**
| Annotation | Use |
|-----------|-----|
| @SpringBootTest | Full application context |
| @WebMvcTest | Controller layer only |
| @DataJpaTest | Repository layer only |
| @MockBean | Replace bean with mock in Spring context |
| @Mock / @InjectMocks | Pure Mockito (no Spring) |

---

## 8. Production Readiness

**Actuator Endpoints:** `/actuator/health`, `/actuator/metrics`, `/actuator/info`, `/actuator/env`

**Logging:**
```yaml
logging:
  level:
    com.supplysync: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**Docker:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Key Production Topics:** Health checks, graceful shutdown, externalized config, secrets management, rate limiting, API versioning, caching (@Cacheable with Redis), async processing (@Async)

---

## Quick Reference: Most Asked Spring Boot Interview Topics

1. Spring Bean lifecycle + scopes
2. Constructor injection vs field injection — why?
3. @Transactional — propagation levels, rollback behavior
4. N+1 problem in JPA — how to solve
5. Spring Security filter chain + JWT implementation
6. Microservices patterns — circuit breaker, saga, API gateway
7. Auto-configuration — how does it work?
8. @SpringBootApplication — what does it do?
9. Profiles and externalized configuration
10. Testing strategies — @WebMvcTest vs @SpringBootTest vs @DataJpaTest

---

*Convert to PDF: `pandoc SPRING_BOOT.md -o SPRING_BOOT.pdf`*
