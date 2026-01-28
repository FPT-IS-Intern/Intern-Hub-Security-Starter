package com.intern.hub.starter.security.dto;

import lombok.Getter;

/**
 * Enumeration representing permission scope levels for access control.
 * <p>
 * Scopes define the extent of data access a user has for a particular resource and action.
 * Higher scope values indicate broader access permissions.
 * </p>
 *
 * <p><b>Scope Hierarchy:</b></p>
 * <ul>
 *   <li>{@link #OWN} (1) - User can only access their own resources</li>
 *   <li>{@link #TENANT} (2) - User can access resources within their tenant/organization</li>
 *   <li>{@link #ALL} (3) - User can access all resources (admin/system level)</li>
 * </ul>
 *
 * <p>Permission checks compare the required scope with the user's scope. Access is granted
 * only if the user's scope value is greater than or equal to the required scope value.</p>
 *
 * @see com.intern.hub.starter.security.annotation.HasPermission
 * @see com.intern.hub.starter.security.context.AuthContext
 */
public enum Scope {

  /**
   * User can only access their own resources.
   */
  OWN(1),

  /**
   * User can access resources within their tenant or organization.
   */
  TENANT(2),

  /**
   * User can access all resources (administrator or system-level access).
   */
  ALL(3);

  /**
   * The numeric value representing the scope level.
   * Higher values indicate broader access permissions.
   */
  @Getter
  private final int value;

  /**
   * Constructs a Scope with the specified numeric value.
   *
   * @param value the numeric value of the scope level
   */
  Scope(int value) {
    this.value = value;
  }

}
