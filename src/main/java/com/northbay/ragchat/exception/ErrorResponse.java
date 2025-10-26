package com.northbay.ragchat.exception;

import java.time.OffsetDateTime;

/**
 * Model class representing a standardized API error response.
 * <p>
 * Contains details such as timestamp, HTTP status code, error type,
 * descriptive message, and the request path where the error occurred.
 */
public class ErrorResponse {
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(int status, String error, String message, String path){
        this.status = status; this.error = error; this.message = message; this.path = path;
    }
    public OffsetDateTime getTimestamp(){return timestamp;}
    public int getStatus(){return status;}
    public String getError(){return error;}
    public String getMessage(){return message;}
    public String getPath(){return path;}
}
