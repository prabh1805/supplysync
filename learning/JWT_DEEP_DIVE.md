# JWT Authentication — Deep Dive

Everything you need to understand about JWT, how it works, why it exists, and how we implemented it in SupplySync.

---

## The Problem: HTTP is Stateless

HTTP doesn't remember anything between requests. Every request is independent. The server has no idea if the person making request #2 is the same person who made request #1.

So how do we "stay logged in"?

---

## Old Way: Sessions

```
┌──────────┐                    ┌──────────────┐
│  Client  │ ── POST /login ──► │    Server    │
│ (Browser)│                    │              │
│          │ ◄── Cookie: ────── │ Creates      │
│          │   sessionId=abc123 │ session in   │
│          │                    │ memory/DB    │
│          │                    │              │
│          │ ── GET /products ─►│ Looks up     │
│          │   Cookie: abc123   │ session abc  │
│          │                    │ "Oh it's     │
│          │ ◄── products ───── │  Prabhat"    │
└──────────┘                    └──────────────┘
```

**How it works:**
1. You login, server creates a session object in memory (or Redis/DB)
2. Server gives you a cookie with the session ID
3. Every request sends the cookie automatically
4. Server looks up the session ID to know who you are

**Why it breaks in microservices:**
- Session is stored on ONE server
- If you have 10 microservices, which one holds the session?
- Sticky sessions? Load balancer complexity? Shared session store?
- It doesn't scale cleanly

---

## New Way: JWT (JSON Web Token)

```
┌──────────┐                    ┌──────────────┐
│  Client  │ ── POST /login ──► │    Server    │
│          │                    │              │
│          │ ◄── { token: ───── │ Creates JWT  │
│          │   "eyJhbG..." }    │ (signed)     │
│          │                    │              │
│          │ ── GET /products ─►│ Validates    │
│          │   Authorization:   │ JWT signature│
│          │   Bearer eyJhbG... │ (no DB call) │
│          │                    │              │
│          │ ◄── products ───── │ "Signature   │
│          │                    │  checks out" │
└──────────┘                    └──────────────┘
```

**Key difference:** The server doesn't store anything. The token itself contains all the info. Any server can verify it because they all share the same secret key.

---

## What's Inside a JWT?

A JWT is a string with 3 parts separated by dots:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcmFiaGF0QGFjbWUuY29tIn0.abc123signature
|_____________________||_________________________________||_________________|
       HEADER                     PAYLOAD                     SIGNATURE
```

### Part 1: Header
```json
{
  "alg": "HS256"
}
```
Tells the receiver which algorithm was used to sign the token. HS256 = HMAC-SHA256.

### Part 2: Payload (Claims)
```json
{
  "sub": "prabhat@acme.com",
  "role": "TENANT_ADMIN",
  "tenantId": "a09666c8-1285-4eff-a6d2-c6f66936351c",
  "iat": 1712275200,
  "exp": 1712361600
}
```

| Claim | Full Name | What It Is |
|-------|-----------|------------|
| `sub` | Subject | Who this token belongs to (usually email or user ID) |
| `iat` | Issued At | Unix timestamp of when the token was created |
| `exp` | Expiration | Unix timestamp of when the token expires |
| `role` | (custom) | User's role — we added this |
| `tenantId` | (custom) | Which tenant this user belongs to — we added this |

Standard claims (sub, iat, exp) are defined by the JWT spec. You can add any custom claims you want.

### Part 3: Signature
```
HMAC-SHA256(
  base64(header) + "." + base64(payload),
  SECRET_KEY
)
```

This is the security part. The server takes the header + payload, hashes them with a secret key, and that hash IS the signature.

**Why this matters:**
- If anyone changes the payload (e.g., changes role from VIEWER to ADMIN), the signature won't match
- Only the server knows the secret key, so only the server can create valid signatures
- The server can verify a token without storing anything — just recalculate the signature and compare

---

## The Complete Authentication Flow

```
                    REGISTRATION
                    ============

Client                                          Auth Service                    Database
  │                                                  │                              │
  │  POST /api/v1/auth/register                      │                              │
  │  { email, password, firstName, tenantId, role }   │                              │
  │─────────────────────────────────────────────────►│                              │
  │                                                  │                              │
  │                                    Validate input │                              │
  │                                    Check if email │  SELECT * FROM users         │
  │                                    already exists │  WHERE email = ?             │
  │                                                  │─────────────────────────────►│
  │                                                  │◄─────────────────────────────│
  │                                                  │  (not found = good)          │
  │                                                  │                              │
  │                                    Hash password  │                              │
  │                                    with BCrypt    │                              │
  │                                    "test123" →    │                              │
  │                                    "$2a$10$xK..." │                              │
  │                                                  │                              │
  │                                    Save user      │  INSERT INTO users           │
  │                                                  │─────────────────────────────►│
  │                                                  │◄─────────────────────────────│
  │                                                  │                              │
  │                                    Generate JWT   │                              │
  │                                    with email,    │                              │
  │                                    role, tenantId │                              │
  │                                                  │                              │
  │  201 Created                                     │                              │
  │  { token: "eyJ...", email, fullName, role }      │                              │
  │◄─────────────────────────────────────────────────│                              │
  │                                                  │                              │


                    LOGIN
                    =====

Client                                          Auth Service                    Database
  │                                                  │                              │
  │  POST /api/v1/auth/login                         │                              │
  │  { email: "prabhat@acme.com", password: "test123" }                             │
  │─────────────────────────────────────────────────►│                              │
  │                                                  │                              │
  │                                    Find user      │  SELECT * FROM users         │
  │                                    by email       │  WHERE email = ?             │
  │                                                  │─────────────────────────────►│
  │                                                  │◄─────────────────────────────│
  │                                                  │  (found: hashed password)    │
  │                                                  │                              │
  │                                    BCrypt.matches( │                              │
  │                                      "test123",   │                              │
  │                                      "$2a$10$xK." │                              │
  │                                    ) → true       │                              │
  │                                                  │                              │
  │                                    Generate JWT   │                              │
  │                                                  │                              │
  │  200 OK                                          │                              │
  │  { token: "eyJ...", email, fullName, role }      │                              │
  │◄─────────────────────────────────────────────────│                              │
  │                                                  │                              │


                    AUTHENTICATED REQUEST
                    =====================

Client                              JwtAuthFilter          SecurityConfig         Controller
  │                                      │                       │                     │
  │  GET /api/v1/products                │                       │                     │
  │  Authorization: Bearer eyJ...        │                       │                     │
  │─────────────────────────────────────►│                       │                     │
  │                                      │                       │                     │
  │                        Extract token │                       │                     │
  │                        from header   │                       │                     │
  │                                      │                       │                     │
  │                        Validate:     │                       │                     │
  │                        - signature   │                       │                     │
  │                        - expiration  │                       │                     │
  │                                      │                       │                     │
  │                        Extract:      │                       │                     │
  │                        - email       │                       │                     │
  │                        - role        │                       │                     │
  │                        - tenantId    │                       │                     │
  │                                      │                       │                     │
  │                        Set Security  │                       │                     │
  │                        Context       │                       │                     │
  │                                      │──── authenticated ──►│                     │
  │                                      │                       │── check rules ────►│
  │                                      │                       │   (authenticated?   │
  │                                      │                       │    yes → allow)     │
  │                                      │                       │                     │
  │  200 OK { products: [...] }          │                       │◄────────────────────│
  │◄─────────────────────────────────────│                       │                     │


                    INVALID/MISSING TOKEN
                    =====================

Client                              JwtAuthFilter          SecurityConfig
  │                                      │                       │
  │  GET /api/v1/products                │                       │
  │  (no Authorization header)           │                       │
  │─────────────────────────────────────►│                       │
  │                                      │                       │
  │                        No token →    │                       │
  │                        skip filter   │                       │
  │                                      │── no authentication ─►│
  │                                      │                       │── check rules
  │                                      │                       │   (authenticated?
  │                                      │                       │    NO → reject)
  │  401 Unauthorized                    │                       │
  │◄─────────────────────────────────────│                       │
```

---

## BCrypt Password Hashing

**Why not store passwords as plain text?**
If your database gets hacked, every user's password is exposed. With hashing, even if the DB is stolen, the passwords are unreadable.

**Why BCrypt specifically?**
- It's slow on purpose — makes brute force attacks impractical
- It adds a random "salt" to each password — same password produces different hashes
- Industry standard for password storage

```
Registration:
  "test123" → BCrypt.encode() → "$2a$10$xK8f3jQ9vN2mR5pL7wY1Oe..."
  (stored in DB)

Login:
  "test123" → BCrypt.matches("test123", "$2a$10$xK8f3jQ9v...") → true
  "wrong"   → BCrypt.matches("wrong",   "$2a$10$xK8f3jQ9v...") → false
```

BCrypt.matches() doesn't decrypt the hash (hashes are one-way). It hashes the input with the same salt and compares the results.

---

## Our Implementation — File by File

---

### 1. `JwtService.java` — Token Factory

This class has one job: create tokens and read tokens.

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;
```

`@Value` injects values from `application.properties`. The secret key and expiration time are configurable, not hardcoded.

```properties
# application.properties
jwt.secret=my-super-secret-key-that-should-be-at-least-256-bits-long-for-hs256-algorithm
jwt.expiration=86400000
```

The secret must be at least 256 bits (32 characters) for HS256. In production, this would come from an environment variable or a secrets manager, never committed to code.

#### Generating a Token

```java
public String generateToken(String email, String role, String tenantId) {
    return Jwts.builder()
            .subject(email)                    // "sub" claim
            .claims(Map.of(                    // custom claims
                    "role", role,
                    "tenantId", tenantId
            ))
            .issuedAt(new Date())              // "iat" claim
            .expiration(new Date(              // "exp" claim
                System.currentTimeMillis() + expiration
            ))
            .signWith(getSigningKey())         // sign with HMAC-SHA256
            .compact();                        // build the string
}
```

Step by step:
1. `Jwts.builder()` — start building a JWT
2. `.subject(email)` — set the "sub" claim (who this token is for)
3. `.claims(Map.of(...))` — add custom data (role, tenantId)
4. `.issuedAt(new Date())` — timestamp of creation
5. `.expiration(...)` — when it expires (current time + 24 hours)
6. `.signWith(getSigningKey())` — create the signature using our secret
7. `.compact()` — produce the final `header.payload.signature` string

#### Validating and Reading a Token

```java
public Claims extractClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())    // use same key to verify
            .build()
            .parseSignedClaims(token)       // parse + validate signature
            .getPayload();                  // return the claims
}
```

This does two things at once:
1. Verifies the signature (if tampered, throws `SignatureException`)
2. Returns the payload claims so we can read email, role, etc.

If the token is invalid (wrong signature, expired, malformed), this throws an exception. Our `isTokenValid()` method catches that:

```java
public boolean isTokenValid(String token) {
    try {
        extractClaims(token);          // throws if invalid
        return !isTokenExpired(token); // check expiry
    } catch (Exception e) {
        return false;                  // any problem = invalid
    }
}
```

---

### 2. `JwtAuthFilter.java` — The Bouncer

This filter runs before every request reaches your controller.

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
```

`OncePerRequestFilter` — Spring guarantees this runs exactly once per request (some filters can accidentally run multiple times due to internal forwarding).

#### The Filter Logic

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {

    // 1. Get the Authorization header
    String authHeader = request.getHeader("Authorization");

    // 2. No token? Let it through
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    // 3. Extract token (remove "Bearer " prefix)
    String token = authHeader.substring(7);

    // 4. Validate and set security context
    if (jwtService.isTokenValid(token)) {
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    // 5. Continue
    filterChain.doFilter(request, response);
}
```

**Why `filterChain.doFilter()`?**
Filters form a chain. Each filter does its job and passes the request to the next filter. If you don't call `filterChain.doFilter()`, the request stops here and never reaches the controller.

**Why `SecurityContextHolder`?**
This is Spring Security's way of knowing "who is the current user?" When you set an `Authentication` object here, Spring Security treats the request as authenticated. Controllers can then access the user info via `SecurityContextHolder.getContext().getAuthentication()`.

**Why `"ROLE_" + role`?**
Spring Security convention. When you check `hasRole("ADMIN")`, Spring internally checks for authority `"ROLE_ADMIN"`. The `ROLE_` prefix is required.

---

### 3. `SecurityConfig.java` — The Rules

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Line by line:

| Line | What It Does |
|------|-------------|
| `@EnableWebSecurity` | Activates Spring Security's web security support |
| `.csrf(disable)` | Disables CSRF protection. CSRF is for cookie-based auth (prevents cross-site form submissions). We use Bearer tokens, not cookies, so CSRF doesn't apply. |
| `.sessionCreationPolicy(STATELESS)` | Tells Spring "don't create HTTP sessions." We don't need them — JWT is our session. Without this, Spring creates a session for every request, wasting memory. |
| `.requestMatchers("/api/v1/auth/**").permitAll()` | These URLs are public. No token needed. The `**` matches anything under `/auth/` (register, login, etc.) |
| `.anyRequest().authenticated()` | Everything else requires a valid JWT token in the Authorization header. |
| `.addFilterBefore(jwtAuthFilter, ...)` | Insert our JwtAuthFilter before Spring's default authentication filter. This way, our filter runs first, validates the JWT, and sets the SecurityContext before Spring checks if the request is authenticated. |
| `PasswordEncoder bean` | Makes BCryptPasswordEncoder available for injection. Used in UserService to hash and verify passwords. |

---

### 4. `UserService.java` — Updated for JWT

#### Registration (what changed)

```java
// BEFORE (plain text)
.password(request.getPassword())

// AFTER (hashed)
.password(passwordEncoder.encode(request.getPassword()))
```

`passwordEncoder.encode("test123")` produces something like `$2a$10$xK8f3jQ9vN2mR5pL7wY1Oe...` — a 60-character BCrypt hash. This is what gets stored in the database. The original password is unrecoverable.

After saving, we generate a token:

```java
String token = jwtService.generateToken(
        user.getEmail(),
        user.getRole().name(),
        user.getTenantId().toString()
);
```

The user gets a token immediately after registration — no need to login separately.

#### Login (new)

```java
public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new InvalidRequestException("Invalid email or password");
    }

    String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole().name(),
            user.getTenantId().toString()
    );

    return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .fullName(user.getFirstName() + " " + user.getLastName())
            .role(user.getRole().name())
            .build();
}
```

Notice: the error message is the same for "email not found" and "wrong password" — `"Invalid email or password"`. This is intentional. If you say "email not found", an attacker knows that email doesn't exist. If you say "wrong password", they know the email IS valid. Generic message = no information leakage.

---

## The Request Lifecycle with JWT

Here's exactly what happens for an authenticated request:

```
1. Client sends:
   GET /api/v1/products
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWI...

2. Request hits Tomcat

3. Spring Security filter chain starts:
   a. SecurityContextHolderFilter
   b. HeaderWriterFilter
   c. LogoutFilter
   d. ★ JwtAuthFilter (OUR FILTER) ★
      - reads Authorization header
      - extracts "eyJhbG..." token
      - calls jwtService.isTokenValid(token)
        - parses token with secret key
        - checks signature matches
        - checks not expired
      - extracts email="prabhat@acme.com", role="TENANT_ADMIN"
      - creates UsernamePasswordAuthenticationToken
      - sets SecurityContextHolder
   e. UsernamePasswordAuthenticationFilter (Spring's default — skipped, we already authenticated)
   f. AuthorizationFilter
      - checks: is this request authenticated? YES (we set it in step d)
      - checks: does the URL pattern match any rules? /api/v1/products → anyRequest().authenticated() → PASS

4. Request reaches ProductController.getProducts()

5. Controller processes and returns response

6. SecurityContextHolder is cleared (per-request lifecycle)
```

---

## Security Considerations

| Concern | How We Handle It |
|---------|-----------------|
| Password storage | BCrypt hashing — never plain text |
| Token tampering | HMAC-SHA256 signature — any change invalidates it |
| Token expiry | 24-hour expiration — limits damage if token is stolen |
| Information leakage | Generic "Invalid email or password" on login failure |
| CSRF | Disabled — not applicable for Bearer token auth |
| Session hijacking | No sessions — STATELESS policy |
| Secret key exposure | Stored in application.properties (should be env variable in production) |

**What we'd add in production:**
- Refresh tokens (short-lived access token + long-lived refresh token)
- Token blacklisting (for logout — invalidate tokens before expiry)
- Rate limiting on login endpoint (prevent brute force)
- Secret key from environment variable or AWS Secrets Manager
- HTTPS only (tokens in plain HTTP can be intercepted)

---

## Dependencies

```groovy
// JWT library — jjwt by io.jsonwebtoken
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'      // interfaces and builder API
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'        // implementation (runtime only)
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'     // JSON serialization for claims
```

Why 3 separate jars? Clean separation of API and implementation. Your code only depends on `jjwt-api` (interfaces). The implementation is loaded at runtime. This is a common pattern in Java libraries.

```groovy
// Spring Security — already included
implementation 'org.springframework.boot:spring-boot-starter-security'
```

This brings in the entire Spring Security framework: filters, SecurityContext, PasswordEncoder, authorization rules, etc.

---

## Quick Reference

**Generate token:**
```java
jwtService.generateToken(email, role, tenantId)
```

**Validate token:**
```java
jwtService.isTokenValid(token) // true or false
```

**Extract data from token:**
```java
jwtService.extractEmail(token)    // "prabhat@acme.com"
jwtService.extractRole(token)     // "TENANT_ADMIN"
jwtService.extractTenantId(token) // "a09666c8-..."
```

**Hash a password:**
```java
passwordEncoder.encode("test123") // "$2a$10$xK8f..."
```

**Verify a password:**
```java
passwordEncoder.matches("test123", "$2a$10$xK8f...") // true
passwordEncoder.matches("wrong",   "$2a$10$xK8f...") // false
```

**Send authenticated request:**
```
GET /api/v1/anything
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
