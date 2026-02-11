# Security Starter Library

[English](./README.md) | [Tiếng Việt](./README.vi.md)

Một starter bảo mật cho Spring Boot cung cấp quản lý ngữ cảnh xác thực (authentication context), kiểm soát truy cập dựa trên quyền (permission-based access control), và xác thực nội bộ giữa các dịch vụ (service-to-service) sử dụng các tính năng hiện đại của Java.

## Tính năng

- 🔐 **Kiểm soát truy cập dựa trên quyền** - Annotation `@HasPermission` mang tính khai báo
- 🔗 **Xác thực dịch vụ nội bộ** - Bảo mật các cuộc gọi giữa các dịch vụ (service-to-service) với annotation `@Internal`
- ⚡ **Hỗ trợ Virtual Thread** - Sử dụng Java `ScopedValue` thay vì `ThreadLocal`
- 🛡️ **Bảo vệ tấn công thời gian (Timing Attack)** - So sánh bí mật (secret) với thời gian không đổi (constant-time)
- 📝 **Tích hợp JPA Auditing** - Tự động theo dõi ID người dùng tạo/sửa đổi
- ⚙️ **Tự động cấu hình Spring Boot** - Thiết lập không cần cấu hình với các mặc định hợp lý

## Yêu cầu

- Java 25+
- Spring Boot 4.0+
- Intern Hub Common Library 2.0.1+

## Cài đặt

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

## Cấu hình

Thêm đoạn sau vào `application.yml` của bạn:

```yaml
security:
  # Bắt buộc: Secret cho xác thực nội bộ giữa các dịch vụ
  internal-secret: "your-secure-secret-key"

  # Tùy chọn: Tiền tố đường dẫn cho các endpoint nội bộ (mặc định: /internal/)
  internal-path-prefix: "/internal/"

  # Tùy chọn: Các đường dẫn cần loại trừ khỏi xử lý bảo mật
  excluded-paths:
    - "/actuator/"
    - "/health"

# Tùy chọn: Cấu hình JPA Auditing
audit:
  data:
    # Bật/tắt JPA auditing (mặc định: true)
    enabled: true
    # ID người dùng mặc định cho các thao tác hệ thống khi không có người dùng xác thực (mặc định: 0)
    default-system-id: 0
```

## Sử dụng

### 1. Bật Bảo mật

Thêm `@EnableSecurity` vào class ứng dụng chính (main application class) của bạn:

```java
@SpringBootApplication
@EnableSecurity
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 2. Kiểm soát truy cập dựa trên quyền

Sử dụng `@HasPermission` để bảo vệ các endpoint với các kiểm tra quyền:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @HasPermission(resource = "user", action = "read")
    public User getUser(@PathVariable Long id) {
        // Người dùng cần quyền 'user:read'
        return userService.findById(id);
    }

    @GetMapping
    @HasPermission(resource = "user", action = "read-all")
    public List<User> getAllUsers() {
        // Người dùng cần quyền 'user:read-all'
        return userService.findAll();
    }
}
```


### 3. Cuộc gọi dịch vụ nội bộ (Internal Service Calls)

Bảo vệ các endpoint cho giao tiếp nội bộ giữa các dịch vụ:

```java
@RestController
@RequestMapping("/internal/sync")
public class InternalSyncController {

    @PostMapping("/users")
    @Internal
    public void syncUsers(@RequestBody List<User> users) {
        // Chỉ có thể truy cập thông qua các cuộc gọi dịch vụ nội bộ
        userService.syncAll(users);
    }
}
```

Để gọi các endpoint nội bộ từ một dịch vụ khác:

```java
// Thêm header X-Internal-Secret
restClient.post()
    .uri("http://user-service/internal/sync/users")
    .header("X-Internal-Secret", internalSecret)
    .body(users)
    .retrieve()
    .toBodilessEntity();
```

### 4. Truy cập Ngữ cảnh Xác thực (Authentication Context)

Truy cập ngữ cảnh xác thực của người dùng hiện tại thông qua lập trình:

```java
@Service
public class MyService {

    public void doSomething() {
        AuthContext context = AuthContextHolder.get()
            .orElseThrow(() -> new UnauthorizedException("Not authenticated"));

        Long userId = context.userId();
        boolean isInternal = context.internal();
        Set<String> permissions = context.permissions();

        // Sử dụng context cho logic nghiệp vụ
    }
}
```

### 5. JPA Auditing

Thư viện cung cấp tính năng JPA auditing tự động theo dõi ai đã tạo hoặc sửa đổi thực thể (entity). Đơn giản chỉ cần kế thừa lớp cơ sở `AuditEntity`:

```java
@Entity
public class Article extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    // getters và setters
}
```

#### Các trường được kế thừa từ AuditEntity

| Trường      | Kiểu   | Mô tả                                    |
| ----------- | ------ | ---------------------------------------- |
| `createdAt` | `Long` | Thời gian entity được tạo (millis)       |
| `updatedAt` | `Long` | Thời gian entity được cập nhật lần cuối  |
| `createdBy` | `Long` | ID người dùng đã tạo entity              |
| `updatedBy` | `Long` | ID người dùng đã sửa đổi entity lần cuối |

#### Cách thức hoạt động

- **Các request đã xác thực**: `createdBy` và `updatedBy` được tự động set thành ID của người dùng đã xác thực từ `AuthContextHolder`
- **Các request chưa xác thực/Hệ thống**: Sử dụng giá trị dự phòng được cấu hình `audit.data.default-system-id` (mặc định: `0`)
- **Tùy chỉnh AuditorAware**: Bạn có thể cung cấp bean `AuditorAware<Long>` của riêng mình để ghi đè hành vi mặc định

#### Tắt Auditing

Để tắt tính năng audit:

```yaml
audit:
  data:
    enabled: false
```

## Header Request

Bộ lọc bảo mật (security filter) đọc các header sau (thường được thiết lập bởi API Gateway):

| Header              | Mô tả                                  | Ví dụ                              |
| ------------------- | -------------------------------------- | ---------------------------------- |
| `X-Authenticated`   | Request có được xác thực hay không     | `true`                             |
| `X-UserId`          | ID của người dùng đã xác thực          | `12345`                            |
| `X-Authorities`     | Các quyền được phân cách bằng dấu phẩy | `user:read:OWN,order:write:TENANT` |
| `X-Internal-Secret` | Secret cho các endpoint nội bộ         | `your-secret-key`                  |

### Định dạng Authority

Authorities tuân theo định dạng: `resource:action:scope`

Ví dụ:

- `user:read:OWN` - Có thể đọc dữ liệu người dùng của chính mình
- `order:write:TENANT` - Có thể ghi đơn hàng trong tenant của họ
- `report:delete:ALL` - Có thể xóa bất kỳ báo cáo nào (admin)

## Kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                        Request                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityFilter                           │
│  • Xác thực secret nội bộ cho endpoint /internal/*          │
│  • Phân tích các header xác thực                            │
│  • Binding AuthContext sử dụng ScopedValue                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityAspect                           │
│  • Chặn các method @HasPermission                           │
│  • Chặn các method @Internal                                │
│  • Xác thực quyền dựa trên AuthContext                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Method                        │
└─────────────────────────────────────────────────────────────┘
```

## Các lưu ý về bảo mật

1. **Secret nội bộ**: Lưu trữ `security.internal-secret` một cách an toàn (ví dụ: biến môi trường, trình quản lý bí mật)
2. **Tấn công thời gian (Timing Attacks)**: Thư viện sử dụng so sánh thời gian không đổi để xác thực secret
3. **Xác thực Header**: Tất cả các header đều được phân tích một cách phòng thủ với xử lý lỗi thích hợp
4. **Virtual Threads**: Sử dụng `ScopedValue` để truyền context an toàn trong luồng (thread-safe)

## Thay đổi phá vỡ (Breaking Changes)

### Loại bỏ cột Version trong AuditEntity

⚠️ **Cảnh báo khi nâng cấp (1.0.2+):** Cột `version` đã bị xóa khỏi lớp cơ sở `AuditEntity`.

**Lý do:** Không phải tất cả các bảng đều yêu cầu cột `version` cho optimistic locking, và việc bắt buộc đưa nó vào gây ra vấn đề hiệu năng trong một số trường hợp.

**Hành động cần thiết:** Nếu entity của bạn yêu cầu optimistic locking, bạn phải thêm trường `@Version` vào lớp entity của bạn một cách rõ ràng.

## Hướng dẫn chuyển đổi

### Từ Common Library

Nếu bạn trước đây đã sử dụng các thành phần bảo mật từ `intern-hub-common-library`:

1. Thêm thư viện bảo mật này như một dependency
2. Cập nhật các import từ `com.intern.hub.library.common.security` thành `com.intern.hub.starter.security`
3. Thay thế `@EnableWebSecurity` bằng `@EnableSecurity`
4. Thêm `security.internal-secret` vào cấu hình của bạn

## Giấy phép

MIT License. Xem file `LICENSE` để biết chi tiết.
