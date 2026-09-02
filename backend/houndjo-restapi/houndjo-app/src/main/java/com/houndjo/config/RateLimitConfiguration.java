package com.houndjo.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Resilience4j rate-limiter instances from {@link ApplicationProperties}.
 */
@Configuration
public class RateLimitConfiguration {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(ApplicationProperties properties) {
        ApplicationProperties.RateLimit rateLimit = properties.getRateLimit();

        RateLimiterConfig authConfig = RateLimiterConfig.custom()
                .limitForPeriod(rateLimit.auth().limitForPeriod())
                .limitRefreshPeriod(rateLimit.auth().limitRefreshPeriod())
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
                .limitForPeriod(rateLimit.api().limitForPeriod())
                .limitRefreshPeriod(rateLimit.api().limitRefreshPeriod())
                .timeoutDuration(Duration.ZERO)
                .build();

        return RateLimiterRegistry.of(Map.of("auth", authConfig, "api", apiConfig));
    }
}
