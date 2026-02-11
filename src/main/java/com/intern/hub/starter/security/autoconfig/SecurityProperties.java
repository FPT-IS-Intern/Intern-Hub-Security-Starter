package com.intern.hub.starter.security.autoconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the security starter.
 * <p>
 * These properties can be configured in your {@code application.yml} or
 * {@code application.properties}:
 * </p>
 *
 * <pre>{@code
 * security:
 *   internal-secret: "your-secret-key"
 *   internal-path-prefix: "/internal/"
 * }</pre>
 *
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

  /**
   * The secret key used for internal service-to-service authentication.
   * <p>
   * This secret must be passed in the {@code X-Internal-Secret} header for
   * requests to internal endpoints.
   * </p>
   */
  @NotBlank(message = "security.internal-secret is required")
  private String internalSecret;

  /**
   * The URI path prefix that identifies internal endpoints.
   * <p>
   * Requests to paths starting with this prefix will require the internal secret
   * header.
   * Defaults to {@code /internal/}.
   * </p>
   */
  private String internalPathPrefix = "/internal/";

  /**
   * List of URI path patterns to exclude from security processing.
   * <p>
   * Requests matching these patterns will bypass the security filter entirely.
   * Useful for health checks, actuator endpoints, etc.
   * </p>
   */
  private List<String> excludedPaths = new ArrayList<>();

}
