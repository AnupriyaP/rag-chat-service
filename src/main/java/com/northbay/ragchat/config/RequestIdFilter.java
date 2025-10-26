package com.northbay.ragchat.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;
/**
 * Servlet filter that attaches a unique request identifier (Request ID)
 * to each incoming HTTP request for logging and tracing purposes.
 * <p>
 * If the client provides an {@code X-Request-Id} header, it is reused;
 * otherwise, a new UUID is generated. The ID is stored in the
 * Mapped Diagnostic Context (MDC) so it appears automatically
 * in all log entries for the current request.
 */
@Component
public class RequestIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            String reqId = request.getHeader("X-Request-Id");
            if (reqId == null || reqId.isEmpty()) reqId = UUID.randomUUID().toString();
            MDC.put("request_id", reqId);
            chain.doFilter(req, res);
        } finally {
            MDC.remove("request_id");
        }
    }
}
