package org.mrshoffen.tasktracker.auth.web;

import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.exception.RefreshTokenExpiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME;
import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.REFRESH_TOKEN_COOKIE_NAME;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
@Slf4j
public class AuthenticationControllerAdvice {

    @ExceptionHandler({InvalidCredentialsException.class,
            InvalidRefreshTokenException.class})
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(Exception e) {
        log.warn("Exception occured: {}", e.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }


    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ProblemDetail> handleRefreshTokenExpired(Exception e) {
        log.warn("Exception occured: {}", e.getMessage());

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie accessTokenCookie = ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());

        return ResponseEntity.status(UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(problem);
    }


}
