package com.northbay.ragchat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.northbay.ragchat.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public final class FilterErrorUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FilterErrorUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Writes a standardized JSON ErrorResponse to the HttpServletResponse.
     * @param response The servlet response object.
     * @param status The HTTP status (e.g., 401, 429).
     * @param message The user-friendly message.
     * @param errorCode The application-specific error code.
     * @param path The request URI.
     * @throws IOException
     */
    public static void writeError(HttpServletResponse response, HttpStatus status, String message, String errorCode, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // NOTE: If ErrorResponse has a dedicated field for errorCode, use it here.
        // Based on your current ErrorResponse model, we'll embed the errorCode in the message for demonstration.
        // A better approach is to modify ErrorResponse to have a 'code' field.
        String finalMessage = String.format("[%s] %s", errorCode, message);

        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                finalMessage,
                path
        );

        // Using Jackson's ObjectMapper to write the JSON response
        response.getWriter().write(MAPPER.writeValueAsString(error));
    }
}