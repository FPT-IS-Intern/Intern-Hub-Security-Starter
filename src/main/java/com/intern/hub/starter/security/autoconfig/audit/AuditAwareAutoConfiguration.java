package com.intern.hub.starter.security.autoconfig.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

/**
 * Auto-configuration for the audit feature.
 * <p>
 * This configuration provides an {@link AuditorAware} bean that integrates
 * with the security context to automatically track who created or modified entities.
 * </p>
 *
 * <p>The auditor is determined by:</p>
 * <ul>
 *   <li>The authenticated user's ID from {@link com.intern.hub.starter.security.context.AuthContextHolder}</li>
 *   <li>The configured {@code audit.data.default-system-id} when no user is authenticated</li>
 * </ul>
 *
 * <p>This configuration can be disabled by setting {@code audit.data.enabled=false}</p>
 *
 * @see AuditDataProperties
 * @see AuditorAwareImpl
 */
@AutoConfiguration
@EnableConfigurationProperties(AuditDataProperties.class)
@ConditionalOnProperty(prefix = "audit.data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAwareAutoConfiguration {

  /**
   * Creates the {@link AuditorAware} bean for JPA auditing.
   *
   * @param auditDataProperties the audit configuration properties
   * @return the auditor aware implementation
   */
  @Bean
  @ConditionalOnMissingBean
  public AuditorAware<Long> auditorAware(AuditDataProperties auditDataProperties) {
    return new AuditorAwareImpl(auditDataProperties);
  }

}
