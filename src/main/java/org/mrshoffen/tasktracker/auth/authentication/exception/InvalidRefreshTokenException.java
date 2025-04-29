package org.mrshoffen.tasktracker.auth.authentication.exception;

public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
