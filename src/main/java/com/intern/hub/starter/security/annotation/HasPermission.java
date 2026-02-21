package com.intern.hub.starter.security.annotation;

import com.intern.hub.starter.security.annotation.aspect.SecurityAspect;
import com.intern.hub.starter.security.entity.Action;

import java.lang.annotation.*;

/**
 * Annotation for declarative permission checking on controller methods.
 * <p>
 * When applied to a method, the {@link SecurityAspect}
 * aspect intercepts the method call and verifies that the current authenticated user
 * has the required permission to perform the specified action on the specified resource.
 * </p>
 *
 * <p>Permissions are checked against the user's permissions stored in the
 * {@link com.intern.hub.starter.security.context.AuthContext}. The permission key is constructed
 * as "{@code resource:action}" and the user's scope must be greater than or equal to the required scope.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * @HasPermission(resource = "user", action = "read", scope = Scope.OWN)
 * public User getUser(Long userId) {
 *     // Method implementation
 * }
 * }</pre>
 *
 * @see SecurityAspect
 * @see com.intern.hub.starter.security.context.AuthContext
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

  /**
   * The resource name for the permission check.
   * <p>This represents the entity or resource type being accessed (e.g., "user", "order", "document").</p>
   *
   * @return the resource name
   */
  String resource();

  /**
   * The action being performed on the resource.
   * <p>This represents the operation type (e.g., "read", "write", "delete", "update").</p>
   *
   * @return the action name
   */
  Action action();

}
