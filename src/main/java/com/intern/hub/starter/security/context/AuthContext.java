package com.intern.hub.starter.security.context;

import com.intern.hub.starter.security.annotation.aspect.SecurityAspect;

import java.util.Set;

/**
 * Immutable record representing the authentication context for the current
 * request.
 * <p>
 * This record holds authentication state including user ID and permissions map,
 * which is used by the {@link SecurityAspect} for permission checking.
 * </p>
 *
 * <p>
 * <b>Permissions Map:</b>
 * </p>
 * <ul>
 * <li>Key format: "{@code resource:action}" (e.g., "user:read",
 * "order:delete")</li>
 * ALL)</li>
 * </ul>
 *
 * @param internal      whether this is an internal service-to-service request
 * @param authenticated whether the user is authenticated
 * @param userId        the unique identifier of the authenticated user (null if
 *                      not authenticated)
 * @param permissions   a map of permission keys to their corresponding scopes
 * @see AuthContextHolder
 */
public record AuthContext(
    boolean internal,
    boolean authenticated,
    Long userId,
    Set<String> permissions) {

  /**
   * Context for internal service-to-service requests.
   * <p>
   * Used when a request is validated via the internal secret header.
   * </p>
   */
  public static final AuthContext INTERNAL_CONTEXT = new AuthContext(
      true,
      false,
      null,
      Set.of());

  /**
   * Context for unauthenticated requests.
   * <p>
   * Used when no authentication headers are present or when authentication fails.
   * </p>
   */
  public static final AuthContext UNAUTHENTICATED_CONTEXT = new AuthContext(
      false,
      false,
      null,
      Set.of());

}
