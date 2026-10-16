// exception/ExternalServiceException.java
package com.mphasis.taskpulse.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}