package org.mrshoffen.tasktracker.auth.registration.exception;

public class EmailUnconfirmedException extends RuntimeException{
    public EmailUnconfirmedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailUnconfirmedException(String message) {
        super(message);
    }
}
