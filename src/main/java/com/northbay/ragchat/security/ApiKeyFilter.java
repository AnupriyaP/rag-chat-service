package com.northbay.ragchat.security;

import com.northbay.ragchat.util.FilterErrorUtil;
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

    @PostConstruct
    public void init() {
        Arrays.stream(Optional.ofNullable(apiKeys).orElse("").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(validKeys::add);
        System.out.println("✅ Loaded API keys: " + validKeys);
        log.info("✅ Loaded API keys: {}", validKeys); // ✅ LOGGED
    }

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
            res.getWriter().write("{\"error\":\"Unauthorized - invalid API key\"}");
            return;
        }

        chain.doFilter(req, res);
    }

}
