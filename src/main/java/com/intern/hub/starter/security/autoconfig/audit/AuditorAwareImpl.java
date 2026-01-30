package com.intern.hub.starter.security.autoconfig.audit;

import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} that provides the current auditor
 * based on the security context.
 * <p>
 * This implementation integrates with {@link AuthContextHolder} to retrieve
 * the authenticated user's ID for JPA auditing purposes.
 * </p>
 *
 * <p>The auditor resolution follows this logic:</p>
 * <ol>
 *   <li>If an authenticated user exists in the context, return their user ID</li>
 *   <li>Otherwise, return the configured default system ID</li>
 * </ol>
 *
 * @see AuditDataProperties
 * @see AuthContextHolder
 */
@Slf4j
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

  private final AuditDataProperties auditDataProperties;

  /**
   * Returns the current auditor (user ID) for JPA auditing.
   *
   * @return an {@link Optional} containing the user ID, or the default system ID if no user is authenticated
   */
  @Override
  public @NullMarked Optional<Long> getCurrentAuditor() {
    AuthContext auditor = AuthContextHolder.get().orElse(null);
    if (auditor == null || auditor.userId() == null) {
      log.debug("No authenticated user found, using default system ID: {}", auditDataProperties.getDefaultSystemId());
      return Optional.of(auditDataProperties.getDefaultSystemId());
    }
    log.debug("Authenticated user found, auto audit using user ID: {}", auditor.userId());
    return Optional.of(auditor.userId());
  }

}
