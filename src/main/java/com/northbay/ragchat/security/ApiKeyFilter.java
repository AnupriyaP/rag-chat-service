package com.northbay.ragchat.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import java.io.IOException;
import java.util.*;
/**
 * Servlet filter for API key authentication.
 * <p>
 * This filter validates the presence and correctness of an API key
 * provided in the request header {@code X-API-KEY}.
 * <p>
 * Public endpoints such as Swagger UI and health checks are excluded
 * from authentication.
 */
@Slf4j // ✅ ADDED Lombok logger
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.api-keys:demo-key}")
    private String apiKeys;

    private final Set<String> validKeys = new HashSet<>();

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
     * Initializes and loads valid API keys from application properties.
     * This method runs automatically after bean creation.
     */
    @PostConstruct
    public void init() {
        Arrays.stream(Optional.ofNullable(apiKeys).orElse("").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(validKeys::add);
        System.out.println("✅ Loaded API keys: " + validKeys);
        log.info("✅ Loaded API keys: {}", validKeys); // ✅ LOGGED
    }

    /**
     * Filters incoming HTTP requests and validates API key authentication.
     * <p>
     * Requests to public endpoints bypass authentication.
     * If the API key is missing or invalid, a 401 Unauthorized response is returned.
     *
     * @param req   the incoming HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // Allow Swagger & public endpoints
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(req, res);
            return;
        }

        // Check API Key
        String key = req.getHeader("X-API-KEY");
        if (key == null || !validKeys.contains(key)) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType("application/json");
            //res.getWriter().write("{\"error\":\"Unauthorized - invalid API key\"}");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid API key");
           // FilterErrorUtil.sendError(res, req, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many requests, slow down.");

            return;
        }

        chain.doFilter(req, res);
    }

}
