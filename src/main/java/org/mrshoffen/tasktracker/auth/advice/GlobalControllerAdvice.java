package org.mrshoffen.tasktracker.auth.advice;

import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.authentication.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.authentication.exception.UnconfirmedRegistrationException;
import org.mrshoffen.tasktracker.auth.registration.exception.UserAlreadyExistsException;
import org.mrshoffen.tasktracker.auth.util.CookieBuilderUtil;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME;
import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.REFRESH_TOKEN_COOKIE_NAME;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String errors = e.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(" | "));
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, errors);
        problemDetail.setTitle("Bad Request");
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler({RefreshTokenExpiredException.class,
            InvalidRefreshTokenException.class})
    public ResponseEntity<ProblemDetail> handleRefreshTokenExpired(Exception e) {
        log.warn("Exception occured: {}", e.getMessage());

        ResponseCookie refreshTokenCookie = CookieBuilderUtil.clear(REFRESH_TOKEN_COOKIE_NAME);
        ResponseCookie accessTokenCookie = CookieBuilderUtil.clear(ACCESS_TOKEN_COOKIE_NAME);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());

        return ResponseEntity.status(UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(problem);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCreds(InvalidCredentialsException e) {
        log.warn("Exception occured: {}", e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExistException(UserAlreadyExistsException e) {
        ProblemDetail problem = generateProblemDetail(CONFLICT, e.getMessage());
        return ResponseEntity.status(CONFLICT).body(problem);
    }


    @ExceptionHandler(UnconfirmedRegistrationException.class)
    public ResponseEntity<ProblemDetail> handleUnconfirmedRegistrationException(UnconfirmedRegistrationException e) {
        ProblemDetail problem = generateProblemDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }

    private ProblemDetail generateProblemDetail(HttpStatus status, String detail) {
        log.warn("Exception thrown: {}", detail);

        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }


}
