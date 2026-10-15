// exception/ConflictException.java
package com.mphasis.taskpulse.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}