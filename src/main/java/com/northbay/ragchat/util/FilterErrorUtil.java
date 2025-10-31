/*
package com.northbay.ragchat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilterErrorUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void sendError(HttpServletResponse res, HttpServletRequest req,
                                 HttpStatus status, String errorCode, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorCode", errorCode);
        body.put("message", message);
        body.put("path", req.getRequestURI());

        mapper.writeValue(res.getOutputStream(), body);
    }
}
*/
