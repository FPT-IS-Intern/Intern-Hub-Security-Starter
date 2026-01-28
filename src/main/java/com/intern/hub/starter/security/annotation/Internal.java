package com.intern.hub.starter.security.annotation;

import com.intern.hub.starter.security.annotation.aspect.SecurityAspect;
import com.intern.hub.starter.security.context.AuthContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for restricting method access to internal service-to-service calls
 * only.
 * <p>
 * When applied to a method, the {@link SecurityAspect} intercepts the method
 * call
 * and verifies that the current request was made with a valid internal secret,
 * meaning {@link AuthContext#internal()} returns {@code true}.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 *
 * <pre>{@code
 * @Internal
 * @GetMapping("/internal/sync-data")
 * public void syncData() {
 *     // Only accessible via internal service calls
 * }
 * }</pre>
 *
 * <p>
 * <b>Note:</b> This annotation should be used in conjunction with endpoints
 * under the configured internal path prefix (default: {@code /internal/}).
 * The {@link com.intern.hub.starter.security.autoconfig.filter.SecurityFilter}
 * validates the {@code X-Internal-Secret} header for such endpoints.
 * </p>
 *
 * @see SecurityAspect
 * @see AuthContext#internal()
 * @see com.intern.hub.starter.security.autoconfig.SecurityProperties#getInternalPathPrefix()
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
}
