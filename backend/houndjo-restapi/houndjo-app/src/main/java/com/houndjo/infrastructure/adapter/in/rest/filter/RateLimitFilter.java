package com.houndjo.infrastructure.adapter.in.rest.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houndjo.config.ApplicationProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that applies per-IP and per-user rate limiting using Resilience4j.
 */
@Component
@RequiredArgsConstructor
@Order(-99) // Runs just after Spring Security's filter chain (which is at -100)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final List<Pattern> AUTH_PATH_PATTERNS = List.of(
            Pattern.compile("/api/auth/.*"),
            Pattern.compile("/api/accounts/register"),
            Pattern.compile("/api/accounts/activation.*"),
            Pattern.compile("/api/accounts/reset-password/.*"),
            Pattern.compile("/api/accounts/recover"),
            Pattern.compile("/api/accounts/invitation/.*"));

    private final ApplicationProperties properties;
    private final RateLimiterRegistry registry;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain)
            throws ServletException, IOException {

        if (!properties.getRateLimit().enabled()) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        boolean isAuthPath = isAuthPath(request.getRequestURI());
        String tier = isAuthPath ? "auth" : "api";

        String ipKey = tier + ":ip:" + ip;
        RateLimiter ipLimiter = registry.rateLimiter(ipKey, tier);
        if (!ipLimiter.acquirePermission()) {
            writeTooManyRequests(response, tier);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userId = jwtAuth.getName();
            String deviceId = getDeviceId(request);
            String userKey = "user:" + userId + ":" + deviceId;
            RateLimiter userLimiter = registry.rateLimiter(userKey, "api");
            if (!userLimiter.acquirePermission()) {
                writeTooManyRequests(response, "api");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAuthPath(String uri) {
        return AUTH_PATH_PATTERNS.stream().anyMatch(p -> p.matcher(uri).matches());
    }

    private String getDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-ID");
        return (deviceId != null && !deviceId.isBlank()) ? deviceId : "default";
    }

    private void writeTooManyRequests(HttpServletResponse response, String tier) throws IOException {
        long retryAfter = "auth".equals(tier)
                ? properties.getRateLimit().auth().limitRefreshPeriod().getSeconds()
                : properties.getRateLimit().api().limitRefreshPeriod().getSeconds();

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 429);
        body.put("title", "Too Many Requests");
        body.put("detail", "Rate limit exceeded. Please try again later.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
