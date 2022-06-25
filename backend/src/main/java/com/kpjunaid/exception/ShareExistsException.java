package com.kpjunaid.exception;

public class ShareExistsException extends RuntimeException {
    public ShareExistsException() {
    }

    public ShareExistsException(String message) {
        super(message);
    }
}
