package com.intern.hub.starter.security.context;

import java.util.Optional;

/**
 * Thread-safe holder for the current {@link AuthContext} using Java's
 * {@link ScopedValue}.
 * <p>
 * This class provides access to the authentication context that has been bound
 * to the current scope. It uses {@link ScopedValue} (introduced in Java 21)
 * which
 * provides a more efficient alternative to {@link ThreadLocal} for virtual
 * threads.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 *
 * <pre>{@code
 * // Binding the context (typically done in a filter or interceptor)
 * ScopedValue.runWhere(AuthContextHolder.AUTH_CONTEXT, authContext, () -> {
 *   // The context is available within this scope
 *   AuthContext ctx = AuthContextHolder.get().orElseThrow();
 * });
 *
 * // Retrieving the context
 * Optional<AuthContext> context = AuthContextHolder.get();
 * }</pre>
 *
 * @see AuthContext
 * @see ScopedValue
 */
public final class AuthContextHolder {

  /**
   * The scoped value holding the current {@link AuthContext}.
   * <p>
   * This should be bound using {@code ScopedValue.runWhere()} or similar methods
   * at the beginning of a request scope.
   * </p>
   */
  public static final ScopedValue<AuthContext> AUTH_CONTEXT = ScopedValue.newInstance();

  private AuthContextHolder() {
  }

  /**
   * Get the current AuthContext from the scoped value.
   *
   * @return an Optional containing the current AuthContext, or empty if null
   * @throws IllegalStateException if no AuthContext has been bound to the current
   *                               scope
   */
  public static Optional<AuthContext> get() {
    if (!AUTH_CONTEXT.isBound()) {
      throw new IllegalStateException("No AuthContext is bound to the current scope");
    }
    return Optional.ofNullable(AUTH_CONTEXT.get());
  }

}
