package com.jongsoft.finance.core.exception;

public class StatusException extends RuntimeException {

    private final int statusCode;

    private StatusException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static StatusException notFound(String message) {
        return new StatusException(404, message);
    }

    public static StatusException badRequest(String message) {
        return new StatusException(400, message);
    }

    public static StatusException internalError(String message) {
        return new StatusException(500, message);
    }

}
