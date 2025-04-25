package org.mrshoffen.tasktracker.auth.web;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.dto.LoginDto;
import org.mrshoffen.tasktracker.auth.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.service.JwtService;
import org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.*;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {


    private final JwtService jwtService;

    private final UserProfileClient userProfileClient;

    @Value("${jwt-user.ttl.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${jwt-user.ttl.access-ttl}")
    private Duration accessTtl;


    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginDto loginDto) {
        String userId;
        try {
            log.info("Attempt to authenticate - trying to validate user in user-profile-ws {}", loginDto);
            userId = userProfileClient.getUserIdByEmailAndPassword(loginDto.email(), loginDto.password());
        } catch (FeignException.NotFound e) {
            log.warn("Failed to fetch user {} from user-profile-ws", loginDto.email());
            throw new InvalidCredentialsException("Неверный логин или пароль", e);
        }

        String refreshToken = jwtService.generateRefreshToken(Map.of("userId", userId, "userEmail", loginDto.email()));
        String accessToken = jwtService.generateAccessToken(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path("/")
                .maxAge(refreshTtl)
                .httpOnly(true)
                .build(); //todo samesite?

        ResponseCookie accessTokenCookie = ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .path("/")
                .maxAge(accessTtl)
                .httpOnly(true)
                .build();

        log.info("Authentication successful. Access and refresh tokens are generated");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString(), accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        log.info("Attempt to refresh access token");
        String newAccessToken = jwtService.generateAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", newAccessToken)
                .path("/")
                .maxAge(accessTtl)
                .httpOnly(true)
                .build();

        log.info("Access token successfully refreshed");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            log.info("Attempt to logout, invalidating refresh token");
            jwtService.addRefreshTokenToBlackList(refreshToken);
        }

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

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }

    @ExceptionHandler({InvalidCredentialsException.class,
            InvalidRefreshTokenException.class,
            RefreshTokenExpiredException.class})
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(Exception e) {
        log.warn("Exception occured: {}", e.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }

}
