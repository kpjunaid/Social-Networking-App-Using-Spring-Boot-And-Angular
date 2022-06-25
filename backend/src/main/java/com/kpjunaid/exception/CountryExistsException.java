package com.kpjunaid.exception;

public class CountryExistsException extends RuntimeException {
    public CountryExistsException() {
    }

    public CountryExistsException(String message) {
        super(message);
    }
}
