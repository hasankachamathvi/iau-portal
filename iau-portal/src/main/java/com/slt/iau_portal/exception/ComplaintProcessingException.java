package com.slt.iau_portal.exception;

public class ComplaintProcessingException extends RuntimeException {
    public ComplaintProcessingException(String message) {
        super(message);
    }

    public ComplaintProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
