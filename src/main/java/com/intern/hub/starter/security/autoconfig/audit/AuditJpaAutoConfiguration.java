package com.intern.hub.starter.security.autoconfig.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Auto-configuration for JPA auditing integration.
 * <p>
 * This configuration enables JPA auditing when:
 * <ul>
 *   <li>Spring Data JPA is on the classpath ({@link EnableJpaAuditing} class is present)</li>
 *   <li>An {@link AuditorAware} bean is available</li>
 *   <li>The audit feature is enabled ({@code audit.data.enabled=true})</li>
 * </ul>
 * </p>
 *
 * <p>
 * When enabled, entities using {@code @CreatedBy} and {@code @LastModifiedBy}
 * annotations will be automatically populated with the current auditor (user ID).
 * </p>
 *
 * <p>Example entity:</p>
 * <pre>
 * {@code
 * @Entity
 * @EntityListeners(AuditingEntityListener.class)
 * public class MyEntity {
 *     @CreatedBy
 *     private Long createdBy;
 *
 *     @LastModifiedBy
 *     private Long lastModifiedBy;
 *
 *     @CreatedDate
 *     private Instant createdDate;
 *
 *     @LastModifiedDate
 *     private Instant lastModifiedDate;
 * }
 * }
 * </pre>
 *
 * @see AuditAwareAutoConfiguration
 * @see AuditorAwareImpl
 */
@AutoConfiguration
@AutoConfigureAfter(AuditAwareAutoConfiguration.class)
@EnableConfigurationProperties(AuditDataProperties.class)
@ConditionalOnClass(EnableJpaAuditing.class)
@ConditionalOnBean(AuditorAware.class)
@ConditionalOnProperty(prefix = "audit.data", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaAuditing
public class AuditJpaAutoConfiguration {
}

