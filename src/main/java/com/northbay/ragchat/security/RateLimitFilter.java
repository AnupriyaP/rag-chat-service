package com.northbay.ragchat.security;

import io.github.bucket4j.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j // ✅ ADDED Lombok logger
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.capacity:100}")
    private long capacity;
    @Value("${app.rate-limit.refill-tokens:100}")
    private long refill;
    @Value("${app.rate-limit.refill-period-seconds:60}")
    private long periodSeconds;

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/v3/api-docs/swagger-config",
            "/webjars",
            "/health",
            "/actuator/health",
            "/error"
    );

    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> {
            Refill refillPolicy = Refill.intervally(refill, Duration.ofSeconds(periodSeconds));
            Bandwidth limit = Bandwidth.classic(capacity, refillPolicy);
            return Bucket4j.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();

        // FIX: Allow Swagger & public endpoints to bypass rate limiting
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(req, res);
            return;
        }

        String key = req.getHeader("X-API-KEY");
        if (key == null) {
            // NOTE: This should technically not be reached if ApiKeyFilter runs first,
            // but is a necessary fallback. The error handling here is already correct (uses sendError).
            log.warn("RateLimitFilter: API key missing for path: {}", path); // ✅ LOGGED
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing API key");
            return;
        }

        Bucket bucket = resolveBucket(key);
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded for key: {}. Path: {}", key, path); // ✅ LOGGED
            // The 429 status is correctly handled via sendError, avoiding the 500 error.
            res.sendError(429,"Too many requests");
        }
    }
}