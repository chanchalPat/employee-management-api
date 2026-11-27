package com.chanchal.employeemanagement.exception;

public class ServiceUnavailableException extends RuntimeException {

    private final int retryAfterSeconds;

    public ServiceUnavailableException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
