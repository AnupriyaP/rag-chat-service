package com.northbay.ragchat.security;
import io.github.bucket4j.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Servlet filter for applying rate limiting using Bucket4j.
 * <p>
 * This filter limits the number of API requests per API key within
 * a given time window to prevent abuse and ensure fair usage.
 *
 * <p>
 * Public endpoints (e.g., Swagger UI, health checks) are excluded
 * from rate limiting.
 */
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

/**
 * Resolves or creates a rate limit bucket for the given API key.
 *
 * @param key the API key identifying a client
 * @return the {@link Bucket} associated with the key
 */
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> {
            Refill refillPolicy = Refill.intervally(refill, Duration.ofSeconds(periodSeconds));
            Bandwidth limit = Bandwidth.classic(capacity, refillPolicy);
            return Bucket4j.builder().addLimit(limit).build();
        });
    }

    /**
     * Applies rate limiting logic for each incoming HTTP request.
     * <p>
     * Requests to public paths bypass rate limiting.
     * Requests without an API key are rejected with a 401 Unauthorized status.
     * Requests exceeding the rate limit receive a 429 Too Many Requests response.
     *
     * @param req   the incoming HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
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
           // FilterErrorUtil.sendError(res, req, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many requests, slow down.");
        }
    }
}