// exception/BadRequestException.java
package com.mphasis.taskpulse.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}