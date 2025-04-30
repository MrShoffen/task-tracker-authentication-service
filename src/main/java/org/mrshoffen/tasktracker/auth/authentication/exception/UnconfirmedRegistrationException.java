package org.mrshoffen.tasktracker.auth.authentication.exception;

public class UnconfirmedRegistrationException extends RuntimeException{
    public UnconfirmedRegistrationException(String message) {
        super(message);
    }
}
