package com.jongsoft.finance.core.exception;

public class StatusException extends RuntimeException {

    private final int statusCode;
    private final String localizationMessage;

    private StatusException(int statusCode, String message, String localizationMessage) {
        super(message);
        this.statusCode = statusCode;
        this.localizationMessage = localizationMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLocalizationMessage() {
        return localizationMessage;
    }

    public static StatusException gone(String message) {
        return new StatusException(410, message, null);
    }

    public static StatusException notFound(String message) {
        return new StatusException(404, message, null);
    }

    public static StatusException badRequest(String message) {
        return new StatusException(400, message, null);
    }

    public static StatusException badRequest(String message, String localizationMessage) {
        return new StatusException(400, message, localizationMessage);
    }

    public static StatusException notAuthorized(String message) {
        return new StatusException(401, message, null);
    }

    public static StatusException forbidden(String message) {
        return new StatusException(403, message, null);
    }

    public static StatusException internalError(String message) {
        return new StatusException(500, message, null);
    }

    public static StatusException internalError(String message, String localizationMessage) {
        return new StatusException(500, message, localizationMessage);
    }
}
