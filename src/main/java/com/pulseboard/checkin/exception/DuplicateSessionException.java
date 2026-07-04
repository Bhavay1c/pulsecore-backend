package com.pulseboard.checkin.exception;

public class DuplicateSessionException extends RuntimeException {
    public DuplicateSessionException(String message) {
        super(message);
    }
}
