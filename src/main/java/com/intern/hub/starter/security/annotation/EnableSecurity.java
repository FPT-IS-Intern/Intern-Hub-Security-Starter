package com.intern.hub.starter.security.annotation;

import com.intern.hub.starter.security.autoconfig.CustomSecurityAutoConfiguration;
import com.intern.hub.starter.security.autoconfig.DefaultSecurityFilterChain;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableWebSecurity
@Import({
    CustomSecurityAutoConfiguration.class,
    DefaultSecurityFilterChain.class
})
public @interface EnableSecurity {
}
