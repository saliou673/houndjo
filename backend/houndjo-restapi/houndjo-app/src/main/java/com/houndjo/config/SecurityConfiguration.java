package com.houndjo.config;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Security configuration: HTTP security, CSRF, session management, and password encoder bean.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(
            new PublicRoute(GET, "/api/swagger-ui/**", "/api/docs/**"),
            new PublicRoute(GET, "/actuator/health"),
            new PublicRoute(POST, "/api/auth/login"),
            new PublicRoute(POST, "/api/auth/refresh"),
            new PublicRoute(POST, "/api/accounts/register"),
            new PublicRoute(GET, "/api/accounts/activation"),
            new PublicRoute(POST, "/api/accounts/activation/resend"),
            new PublicRoute(POST, "/api/accounts/recover"),
            new PublicRoute(POST, "/api/accounts/reset-password/init"),
            new PublicRoute(POST, "/api/accounts/reset-password/finish"),
            new PublicRoute(POST, "/api/accounts/invitation/complete"),
            new PublicRoute(POST, "/api/auth/2fa/verify"),
            new PublicRoute(POST, "/api/contact"),
            new PublicRoute(POST, "/api/organizations/invitations/accept"));

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    PUBLIC_ROUTES.forEach(route -> auth.requestMatchers(route.method(), route.patterns())
                            .permitAll());
                    auth.requestMatchers("/**").authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    public CorsFilter corsFilter(ApplicationProperties applicationProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        ApplicationProperties.Cors cors = applicationProperties.getSecurity().cors();

        if (cors != null && !CollectionUtils.isEmpty(cors.allowedOrigins())) {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(cors.allowedOrigins());
            config.setAllowedMethods(
                    List.of(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name(), PATCH.name()));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            source.registerCorsConfiguration("/**", config);
        }

        return new CorsFilter(source);
    }

    private record PublicRoute(HttpMethod method, String... patterns) {}
}
