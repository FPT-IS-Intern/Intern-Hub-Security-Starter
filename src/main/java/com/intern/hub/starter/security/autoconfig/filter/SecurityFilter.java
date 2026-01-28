package com.intern.hub.starter.security.autoconfig.filter;

import com.intern.hub.library.common.context.RequestContextHolder;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.dto.ResponseMetadata;
import com.intern.hub.library.common.dto.ResponseStatus;
import com.intern.hub.library.common.exception.ExceptionConstant;
import com.intern.hub.starter.security.autoconfig.SecurityProperties;
import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import com.intern.hub.starter.security.dto.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Security filter that handles authentication context propagation and internal
 * endpoint protection.
 * <p>
 * This filter runs once per request and:
 * <ul>
 * <li>Validates internal secret for requests to internal endpoints</li>
 * <li>Populates {@link AuthContext} from request headers</li>
 * <li>Binds the context using {@link ScopedValue} for virtual thread
 * safety</li>
 * </ul>
 * </p>
 *
 * @see SecurityProperties
 * @see AuthContext
 * @see AuthContextHolder
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter implements Ordered {

  private final SecurityProperties securityProperties;
  private final ObjectMapper objectMapper;

  private static final ResponseStatus FORBIDDEN_RESPONSE_STATUS = new ResponseStatus(
      ExceptionConstant.FORBIDDEN_DEFAULT_CODE,
      "Forbidden: Invalid internal secret");

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws IOException {
    String uri = request.getRequestURI();

    if (isExcludedPath(uri)) {
      next(request, response, AuthContext.UNAUTHENTICATED_CONTEXT, filterChain);
      return;
    }

    if (uri.startsWith(securityProperties.getInternalPathPrefix())) {
      String internalSecret = request.getHeader("X-Internal-Secret");
      if (!isCorrectInternalSecret(internalSecret)) {
        log.warn("Invalid internal secret for request to: {}", uri);
        responseForbidden(response);
        return;
      }
      log.debug("Internal access granted for: {}", uri);
      next(request, response, AuthContext.INTERNAL_CONTEXT, filterChain);
      return;
    }

    String authenticated = request.getHeader("X-Authenticated");
    if (authenticated == null || !authenticated.equalsIgnoreCase("true")) {
      next(request, response, AuthContext.UNAUTHENTICATED_CONTEXT, filterChain);
      return;
    }

    AuthContext authContext = populateAuthContext(request);
    if (authContext.authenticated()) {
      log.debug("Authenticated user {} accessing: {}", authContext.userId(), uri);
    }
    next(request, response, authContext, filterChain);
  }

  private boolean isExcludedPath(String uri) {
    return securityProperties.getExcludedPaths().stream()
        .anyMatch(uri::startsWith);
  }

  private boolean isCorrectInternalSecret(String internalSecret) {
    if (internalSecret == null) {
      return false;
    }
    byte[] provided = internalSecret.getBytes(StandardCharsets.UTF_8);
    byte[] expected = securityProperties.getInternalSecret().getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(provided, expected);
  }

  private AuthContext populateAuthContext(HttpServletRequest request) {
    String userIdHeader = request.getHeader("X-UserId");
    String authoritiesHeader = request.getHeader("X-Authorities");

    Long userId = parseUserId(userIdHeader);
    if (userId == null || authoritiesHeader == null) {
      return AuthContext.UNAUTHENTICATED_CONTEXT;
    }

    Map<String, Scope> authorityMap = parseAuthorities(authoritiesHeader);
    return new AuthContext(false, true, userId, Collections.unmodifiableMap(authorityMap));
  }

  private Long parseUserId(String userIdHeader) {
    if (userIdHeader == null || userIdHeader.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(userIdHeader);
    } catch (NumberFormatException e) {
      log.warn("Invalid X-UserId header value: {}", userIdHeader);
      return null;
    }
  }

  private Map<String, Scope> parseAuthorities(String authoritiesHeader) {
    String[] authorities = authoritiesHeader.split(",");
    Map<String, Scope> authorityMap = new HashMap<>(authorities.length);
    for (String authority : authorities) {
      String[] parts = authority.split(":");
      if (parts.length != 3) {
        log.debug("Skipping malformed authority: {}", authority);
        continue;
      }
      try {
        Scope scope = Scope.valueOf(parts[2].toUpperCase());
        authorityMap.put(parts[0] + ":" + parts[1], scope);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid scope value in authority: {}", authority);
      }
    }
    return authorityMap;
  }

  private void responseForbidden(HttpServletResponse response) throws IOException {
    ResponseMetadata metadata = null;
    if (RequestContextHolder.REQUEST_CONTEXT.isBound()) {
      metadata = new ResponseMetadata(
          RequestContextHolder.get().requestId(),
          RequestContextHolder.get().traceId(),
          null, System.currentTimeMillis());
    }
    String responseBody = objectMapper.writeValueAsString(
        ResponseApi.of(FORBIDDEN_RESPONSE_STATUS, null, metadata));
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    PrintWriter writer = response.getWriter();
    writer.write(responseBody);
    writer.flush();
  }

  private void next(HttpServletRequest request,
                    HttpServletResponse response,
                    AuthContext authContext,
                    FilterChain filterChain) {
    ScopedValue.where(AuthContextHolder.AUTH_CONTEXT, authContext).run(() -> {
      try {
        filterChain.doFilter(request, response);
      } catch (IOException | ServletException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }

}
