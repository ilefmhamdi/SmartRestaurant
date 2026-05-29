package com.noir.exception;

public class AppException extends RuntimeException {
    private final int statusCode;
    private final Object details;

    public AppException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.details = null;
    }

    public AppException(String message, int statusCode, Object details) {
        super(message);
        this.statusCode = statusCode;
        this.details = details;
    }

    public int getStatusCode() { return statusCode; }
    public Object getDetails() { return details; }
}