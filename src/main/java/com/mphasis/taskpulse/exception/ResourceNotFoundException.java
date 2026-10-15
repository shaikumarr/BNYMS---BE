// exception/ResourceNotFoundException.java
package com.mphasis.taskpulse.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}