package com.northbay.ragchat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("api-logger");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap both request and response so content can be read later
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - start;

            String requestBody = getContentAsString(requestWrapper.getContentAsByteArray());
            String responseBody = getContentAsString(responseWrapper.getContentAsByteArray());

            log.info(
                    "timestamp={} method={} path={} status={} duration_ms={} requestBody={} responseBody={}",
                    Instant.now(),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    truncate(requestBody),
                    truncate(responseBody)
            );

            responseWrapper.copyBodyToResponse(); // send response back
        }
    }

    private String getContentAsString(byte[] content) {
        if (content == null || content.length == 0) return "";
        return new String(content, StandardCharsets.UTF_8)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String truncate(String data) {
        if (data == null) return "";
        return data.length() > 1000 ? data.substring(0, 1000) + "...(truncated)" : data;
    }
}
