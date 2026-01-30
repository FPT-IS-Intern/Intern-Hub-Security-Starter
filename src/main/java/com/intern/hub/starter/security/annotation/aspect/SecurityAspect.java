package com.intern.hub.starter.security.annotation.aspect;

import com.intern.hub.library.common.exception.ExceptionConstant;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.starter.security.annotation.HasPermission;
import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import com.intern.hub.starter.security.dto.Scope;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * Aspect that enforces permission checking for methods annotated with
 * {@link HasPermission}.
 * <p>
 * This aspect intercepts method calls annotated with {@code @HasPermission} and
 * verifies
 * that the current user (obtained from {@link AuthContextHolder}) has
 * sufficient permissions
 * to execute the method.
 * </p>
 *
 * <p>
 * <b>Permission checking logic:</b>
 * </p>
 * <ol>
 * <li>Retrieves the current {@link AuthContext} from
 * {@link AuthContextHolder}</li>
 * <li>Constructs the permission key as "{@code resource:action}"</li>
 * <li>Compares the user's scope with the required scope</li>
 * <li>Throws {@link ForbiddenException} if the user lacks permission</li>
 * </ol>
 *
 * @see HasPermission
 * @see AuthContext
 * @see AuthContextHolder
 * @see ForbiddenException
 */
@Slf4j
@Aspect
public class SecurityAspect {

  /**
   * Around advice that checks permissions before method execution.
   * <p>
   * If the user lacks the required permission, a {@link ForbiddenException} is
   * thrown.
   * Otherwise, the method proceeds normally.
   * </p>
   *
   * @param pjp the proceeding join point representing the intercepted method
   * @return the result of the method execution
   * @throws ForbiddenException if the user is not authenticated or lacks the
   *                            required permission
   */
  @Around("@annotation(com.intern.hub.starter.security.annotation.HasPermission)")
  public Object hasPermissionAdvice(@NonNull ProceedingJoinPoint pjp) {
    MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
    HasPermission hasPermission = methodSignature.getMethod().getAnnotation(HasPermission.class);

    AuthContext authContext = AuthContextHolder.get().orElse(null);
    if (authContext == null) {
      log.debug("No AuthContext found in AuthContextHolder");
      throw new ForbiddenException(ExceptionConstant.FORBIDDEN_DEFAULT_CODE);
    }

    Map<String, Scope> permissions = authContext.permissions();
    Scope userScope = permissions.get(hasPermission.resource() + ":" + hasPermission.action());
    if (userScope == null || hasPermission.scope().getValue() > userScope.getValue()) {
      log.debug("User scope {} is insufficient for required scope {} on resource {} and action {}",
          userScope,
          hasPermission.scope(),
          hasPermission.resource(),
          hasPermission.action());
      throw new ForbiddenException(ExceptionConstant.FORBIDDEN_DEFAULT_CODE);
    }
    return next(pjp);
  }

  /**
   * Around advice that checks for internal access before method execution.
   *
   * @param pjp the proceeding join point
   * @return the result of the method execution
   */
  @Around("@annotation(com.intern.hub.starter.security.annotation.Internal)")
  public Object isInternal(@NonNull ProceedingJoinPoint pjp) {
    AuthContext authContext = AuthContextHolder.get().orElse(null);
    if (authContext == null || !authContext.internal()) {
      log.debug("Access denied: method is marked as internal but AuthContext is missing or not internal");
      throw new ForbiddenException(ExceptionConstant.FORBIDDEN_DEFAULT_CODE);
    }
    return next(pjp);
  }

  private Object next(ProceedingJoinPoint pjp) {
    try {
      return pjp.proceed();
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
