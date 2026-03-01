package com.intern.hub.starter.security.autoconfig;

import com.intern.hub.starter.security.annotation.aspect.SecurityAspect;
import com.intern.hub.starter.security.autoconfig.filter.SecurityFilter;
import io.opentelemetry.api.trace.SpanContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tools.jackson.databind.ObjectMapper;

/**
 * Auto-configuration for the security starter.
 * <p>
 * This configuration provides:
 * <ul>
 * <li>{@link SecurityFilter} - Request filter for authentication context
 * propagation</li>
 * <li>{@link SecurityAspect} - AOP aspect for permission checking</li>
 * </ul>
 * </p>
 *
 * @see SecurityProperties
 * @see SecurityFilter
 * @see SecurityAspect
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CustomSecurityAutoConfiguration {

  @Bean
  public SecurityFilter securityFilter(SecurityProperties securityProperties,
                                       ObjectProvider<ObjectMapper> objectMapperProvider) {
    ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
    return new SecurityFilter(securityProperties, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean(SecurityAspect.class)
  public SecurityAspect securityAspect() {
    return new SecurityAspect();
  }

  @Bean
  @ConditionalOnBean(SpanContext.class)
  public UserIdSpanProcessor userIdSpanProcessor() {
    return new UserIdSpanProcessor();
  }

}