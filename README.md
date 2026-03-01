# Security Starter Library

[English](./README.md) | [Tiếng Việt](./README.vi.md)

A Spring Boot security starter that provides authentication context management, permission-based access control, and internal service-to-service authentication using modern Java features.
 
## Features

- 🔐 **Permission-based Access Control** - Declarative `@HasPermission` annotation
- 🔑 **Authentication Guard** - Declarative `@Authenticated` annotation
- 🔗 **Internal Service Authentication** - Secure service-to-service calls with `@Internal` annotation
- ⚡ **Virtual Thread Support** - Uses Java `ScopedValue` instead of `ThreadLocal`
- 🛡️ **Timing Attack Protection** - Constant-time secret comparison
- 📝 **JPA Auditing Integration** - Automatic tracking of created/modified by user ID
- 🔭 **OpenTelemetry Integration** - Automatic `enduser.id` span attribute via `UserIdSpanProcessor`
- 📊 **MDC Logging** - Automatic `userId` MDC key injection for structured logging
- ⚙️ **Spring Boot Auto-configuration** - Zero-config setup with sensible defaults

## Requirements

- Java 25+
- Spring Boot 4.0+
- Intern Hub Common Library 2.0.4+

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.FPT-IS-Intern:Intern-Hub-Security-Starter:1.0.7")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.FPT-IS-Intern</groupId>
    <artifactId>Intern-Hub-Security-Library</artifactId>
    <version>1.0.7</version>
</dependency>
```

## Configuration

Add the following to your `application.yml`:

```yaml
security:
  # Required: Secret for internal service-to-service authentication
  internal-secret: "your-secure-secret-key"

  # Optional: Path prefix for internal endpoints (default: /internal/)
  internal-path-prefix: "/internal/"

  # Optional: Paths to exclude from security processing
  excluded-paths:
    - "/actuator/"
    - "/health"

# Optional: JPA Auditing configuration
audit:
  data:
    # Enable/disable JPA auditing (default: true)
    enabled: true
    # Default user ID for system operations when no user is authenticated (default: 0)
    default-system-id: 0
```

## Usage

### 1. Enable Security

Add `@EnableSecurity` to your main application class:

```java
@SpringBootApplication
@EnableSecurity
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 2. Permission-Based Access Control

Use `@HasPermission` to protect endpoints with permission checks. The `action` field accepts a value from the `Action` enum:

| `Action` value | Meaning       |
| -------------- | ------------- |
| `Action.CREATE` | Create a resource |
| `Action.READ`   | Read / view a resource |
| `Action.UPDATE` | Update a resource |
| `Action.DELETE` | Delete a resource |
| `Action.REVIEW` | Review a resource |

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @HasPermission(resource = "user", action = Action.READ)
    public User getUser(@PathVariable Long id) {
        // User needs 'user:read' permission
        return userService.findById(id);
    }

    @PostMapping
    @HasPermission(resource = "user", action = Action.CREATE)
    public User createUser(@RequestBody CreateUserRequest request) {
        // User needs 'user:create' permission
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @HasPermission(resource = "user", action = Action.UPDATE)
    public User updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        // User needs 'user:update' permission
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @HasPermission(resource = "user", action = Action.DELETE)
    public void deleteUser(@PathVariable Long id) {
        // User needs 'user:delete' permission
        userService.delete(id);
    }
}
```

### 3. Authentication Guard

Use `@Authenticated` to ensure the caller is authenticated without checking a specific permission:

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping
    @Authenticated
    public Profile getProfile() {
        // Any authenticated user can access this
        AuthContext context = AuthContextHolder.get().orElseThrow();
        return profileService.findByUserId(context.userId());
    }
}
```

### 4. Internal Service Calls

Protect endpoints for internal service-to-service communication:

```java
@RestController
@RequestMapping("/internal/sync")
public class InternalSyncController {

    @PostMapping("/users")
    @Internal
    public void syncUsers(@RequestBody List<User> users) {
        // Only accessible via internal service calls
        userService.syncAll(users);
    }
}
```

To call internal endpoints from another service:

```java
// Add the X-Internal-Secret header
restClient.post()
    .uri("http://user-service/internal/sync/users")
    .header("X-Internal-Secret", internalSecret)
    .body(users)
    .retrieve()
    .toBodilessEntity();
```

### 5. Accessing Authentication Context

Access the current user's authentication context programmatically:

```java
@Service
public class MyService {

    public void doSomething() {
        AuthContext context = AuthContextHolder.get()
            .orElseThrow(() -> new UnauthorizedException("Not authenticated"));

        Long userId = context.userId();
        boolean isInternal = context.internal();
        Set<String> permissions = context.permissions();

        // Use context for business logic
    }
}
```

### 6. JPA Auditing

The library provides automatic JPA auditing that tracks who created or modified entities. Simply extend the `AuditEntity` base class:

```java
@Entity
public class Article extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    // getters and setters
}
```

#### Inherited Fields from AuditEntity

| Field       | Type   | Description                                |
| ----------- | ------ | ------------------------------------------ |
| `createdAt` | `Long` | Timestamp when entity was created (millis) |
| `updatedAt` | `Long` | Timestamp when entity was last updated     |
| `createdBy` | `Long` | User ID who created the entity             |
| `updatedBy` | `Long` | User ID who last modified the entity       |

#### How It Works

- **Authenticated requests**: `createdBy` and `updatedBy` are automatically set to the authenticated user's ID from `AuthContextHolder`
- **Unauthenticated/System requests**: Falls back to the configured `audit.data.default-system-id` (default: `0`)
- **Custom AuditorAware**: You can provide your own `AuditorAware<Long>` bean to override the default behavior

#### Disabling Auditing

To disable the audit feature:

```yaml
audit:
  data:
    enabled: false
```

### 7. OpenTelemetry Integration

When `io.opentelemetry:opentelemetry-sdk-trace` is present on the classpath and a `SpanContext` bean is registered (e.g., via Spring Boot's OpenTelemetry auto-configuration), the library automatically registers a `UserIdSpanProcessor` that enriches every span with the authenticated user's ID.

The processor adds the following attribute to every started span when a user is authenticated:

| Attribute     | Type   | Description                          |
| ------------- | ------ | ------------------------------------ |
| `enduser.id`  | `Long` | The authenticated user's ID          |

No additional configuration is required — the processor is auto-configured via `CustomSecurityAutoConfiguration`.

> **Note:** If OpenTelemetry is not on your classpath, this feature is silently skipped.

### 8. MDC Logging

Starting from version **1.0.7**, the `SecurityFilter` automatically injects the current user's ID into the [SLF4J MDC](https://logback.qos.ch/manual/mdc.html) for every request. This allows you to include the user ID in all log statements without any extra code.

| MDC Key    | Value                                        |
| ---------- | -------------------------------------------- |
| `userId`   | Authenticated user ID, or `anonymous`        |

To include it in your log pattern, add `%X{userId}` to your Logback/Log4j2 configuration:

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] [userId=%X{userId}] %-5level %logger{36} - %msg%n</pattern>
```

## Request Headers

The security filter reads the following headers (typically set by an API Gateway):

| Header              | Description                          | Example                      |
| ------------------- | ------------------------------------ | ---------------------------- |
| `X-Authenticated`   | Whether the request is authenticated | `true`                       |
| `X-UserId`          | The authenticated user's ID          | `12345`                      |
| `X-Authorities`     | Comma-separated permissions          | `user:read,order:create`     |
| `X-Internal-Secret` | Secret for internal endpoints        | `your-secret-key`            |

### Authority Format

Authorities follow the format: `resource:action`

The `action` segment must match one of the `Action` enum values (lowercase):

| Value      | Enum Constant   |
| ---------- | --------------- |
| `create`   | `Action.CREATE` |
| `read`     | `Action.READ`   |
| `update`   | `Action.UPDATE` |
| `delete`   | `Action.DELETE` |
| `review`   | `Action.REVIEW` |

Examples:

- `user:read` - Can read user data
- `order:create` - Can create orders
- `report:delete` - Can delete reports
- `task:review` - Can review tasks

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Request                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityFilter                           │
│  • Validates internal secret for /internal/* endpoints      │
│  • Parses authentication headers                            │
│  • Binds AuthContext using ScopedValue                      │
│  • Injects userId into MDC for structured logging           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityAspect                           │
│  • Intercepts @HasPermission methods                        │
│  • Intercepts @Authenticated methods                        │
│  • Intercepts @Internal methods                             │
│  • Validates permissions against AuthContext                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Method                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│             UserIdSpanProcessor (optional)                  │
│  • Activated when OpenTelemetry SDK is on the classpath     │
│  • Adds enduser.id attribute to every started span          │
│  • Reads userId from AuthContextHolder (ScopedValue)        │
└─────────────────────────────────────────────────────────────┘
```

## Security Considerations

1. **Internal Secret**: Store `security.internal-secret` securely (e.g., environment variable, secrets manager)
2. **Timing Attacks**: The library uses constant-time comparison for secret validation
3. **Header Validation**: All headers are defensively parsed with proper error handling
4. **Virtual Threads**: Uses `ScopedValue` for thread-safe context propagation

## Breaking Changes

### Version 1.0.7 – OpenTelemetry Integration & MDC Logging

#### Common Library Minimum Version Bumped to 2.0.4

⚠️ **Breaking change when upgrading to 1.0.7+:** The minimum required version of `Intern-Hub-Common-Library` has been raised from `2.0.3` to `2.0.4`.

**Action Required:** Ensure your project declares `Intern-Hub-Common-Library:2.0.4` or later as a dependency before upgrading.

#### New Automatic MDC `userId` Key

⚠️ **Potential conflict when upgrading to 1.0.7+:** The `SecurityFilter` now automatically injects a `userId` key into the SLF4J MDC for every request (set to the authenticated user's numeric ID, or `anonymous` for unauthenticated requests). The key is cleared after the request completes.

If your application already populates an MDC key named `userId` via a custom filter, interceptor, or log configuration, the value set by the security filter will overwrite it for the duration of the request.

**Action Required:** Rename any conflicting `userId` MDC key in your custom code, or let the library manage this key exclusively.

### Version 1.0.5 – Authority Format Change

⚠️ **Breaking change when upgrading to 1.0.5+:** The authority format has been simplified and the `@HasPermission` annotation now uses a typed `Action` enum.

**Old format (≤ 1.0.4):**
- Authority string: `resource:action:scope` (e.g., `user:read:OWN`, `order:write:TENANT`)
- Annotation: `@HasPermission(resource = "user", action = "read", scope = Scope.OWN)`

**New format (1.0.5+):**
- Authority string: `resource:action` (e.g., `user:read`, `order:create`)
- Annotation: `@HasPermission(resource = "user", action = Action.READ)`

**Action Required:**
1. Update the `X-Authorities` header sent by your API Gateway — remove the scope segment.
2. Replace all `@HasPermission` usages to use the `Action` enum and remove the `scope` attribute.
3. Replace any string-based action values with the corresponding `Action` enum constant.

### Version Column Removal in AuditEntity

⚠️ **Caution when upgrading (1.0.2+):** The `version` column has been removed from the base `AuditEntity` class.

**Reason:** Not all tables require a `version` column for optimistic locking, and its mandatory inclusion was causing performance issues in some scenarios.

**Action Required:** If your entity requires optimistic locking, you must now explicitly add the `@Version` field to your entity class.

## Migration Guide

### From Common Library

If you were previously using security components from `intern-hub-common-library`:

1. Add this security library as a dependency
2. Update imports from `com.intern.hub.library.common.security` to `com.intern.hub.starter.security`
3. Replace `@EnableWebSecurity` with `@EnableSecurity`
4. Add `security.internal-secret` to your configuration

## License

MIT License. See `LICENSE` file for details.
