package org.mrshoffen.tasktracker.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
