package com.intern.hub.starter.security.autoconfig.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the audit feature.
 * <p>
 * These properties can be configured in {@code application.yml} or {@code application.properties}
 * under the {@code audit.data} prefix.
 * </p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * audit:
 *   data:
 *     enabled: true
 *     default-system-id: 0
 * </pre>
 *
 * @see AuditAwareAutoConfiguration
 * @see AuditorAwareImpl
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "audit.data")
public class AuditDataProperties {

  /**
   * Enable or disable the audit feature.
   * When disabled, the AuditorAware bean will not be created.
   * Default: {@code true}
   */
  private boolean enabled = true;

  /**
   * Default system ID used as the auditor when no authenticated user is present.
   * This value is returned when:
   * <ul>
   *   <li>No authentication context exists</li>
   *   <li>The user ID in the authentication context is null</li>
   * </ul>
   * Default: {@code 0L}
   */
  private Long defaultSystemId = 0L;

}
