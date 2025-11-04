package com.northbay.ragchat.exception;

/**
 * Custom runtime exception class for handling API-specific errors.
 * <p>
 * Encapsulates an error code along with a descriptive message
 * to provide more detailed error information to clients.
 */
public class ApiException extends RuntimeException {
    private final String errorCode;

    public ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // ✅ Constructor: message only
    public ApiException(String message) {
        super(message);
        this.errorCode = null;
    }

    // ✅ Constructor: message + cause (for wrapping other exceptions)
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    // ✅ Constructor: errorCode + message + cause
    public ApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
