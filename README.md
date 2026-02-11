# Security Starter Library

[English](./README.md) | [Tiếng Việt](./README.vi.md)

A Spring Boot security starter that provides authentication context management, permission-based access control, and internal service-to-service authentication using modern Java features.
 
## Features

- 🔐 **Permission-based Access Control** - Declarative `@HasPermission` annotation
- 🔗 **Internal Service Authentication** - Secure service-to-service calls with `@Internal` annotation
- ⚡ **Virtual Thread Support** - Uses Java `ScopedValue` instead of `ThreadLocal`
- 🛡️ **Timing Attack Protection** - Constant-time secret comparison
- 📝 **JPA Auditing Integration** - Automatic tracking of created/modified by user ID
- ⚙️ **Spring Boot Auto-configuration** - Zero-config setup with sensible defaults

## Requirements

- Java 25+
- Spring Boot 4.0+
- Intern Hub Common Library 2.0.2+

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.FPT-IS-Intern:Intern-Hub-Security-Starter:1.0.4")
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
    <version>1.0.4</version>
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

Use `@HasPermission` to protect endpoints with permission checks:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @HasPermission(resource = "user", action = "read")
    public User getUser(@PathVariable Long id) {
        // User needs 'user:read' permission
        return userService.findById(id);
    }

    @GetMapping
    @HasPermission(resource = "user", action = "list")
    public List<User> getAllUsers() {
        // User needs 'user:list' permission
        return userService.findAll();
    }
}
```


### 3. Internal Service Calls

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

### 4. Accessing Authentication Context

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

### 5. JPA Auditing

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

## Request Headers

The security filter reads the following headers (typically set by an API Gateway):

| Header              | Description                          | Example                            |
| ------------------- | ------------------------------------ | ---------------------------------- |
| `X-Authenticated`   | Whether the request is authenticated | `true`                             |
| `X-UserId`          | The authenticated user's ID          | `12345`                            |
| `X-Authorities`     | Comma-separated permissions          | `user:read:OWN,order:write:TENANT` |
| `X-Internal-Secret` | Secret for internal endpoints        | `your-secret-key`                  |

### Authority Format

Authorities follow the format: `resource:action:scope`

Examples:

- `user:read:OWN` - Can read own user data
- `order:write:TENANT` - Can write orders within their tenant
- `report:delete:ALL` - Can delete any report (admin)

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
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityAspect                           │
│  • Intercepts @HasPermission methods                        │
│  • Intercepts @Internal methods                             │
│  • Validates permissions against AuthContext                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Method                        │
└─────────────────────────────────────────────────────────────┘
```

## Security Considerations

1. **Internal Secret**: Store `security.internal-secret` securely (e.g., environment variable, secrets manager)
2. **Timing Attacks**: The library uses constant-time comparison for secret validation
3. **Header Validation**: All headers are defensively parsed with proper error handling
4. **Virtual Threads**: Uses `ScopedValue` for thread-safe context propagation

## Breaking Changes

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
