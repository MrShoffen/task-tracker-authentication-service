package org.mrshoffen.tasktracker.auth.authentication.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.dto.LoginDto;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.authentication.service.AuthenticationService;
import org.mrshoffen.tasktracker.auth.jwt.JwtUtil;
import org.mrshoffen.tasktracker.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME;
import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final JwtUtil jwtUtil;

    private final AuthenticationService authenticationService;

    @Value("${jwt-user.ttl.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${jwt-user.ttl.access-ttl}")
    private Duration accessTtl;

    @PostMapping("/sign-in")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginDto loginDto) {
        authenticationService.validateCredentialsAndGetUserId(loginDto);
        String userId = authenticationService.getUserId(loginDto.email());

        String refreshToken = jwtUtil.generateRefreshToken(Map.of("userId", userId, "userEmail", loginDto.email()));
        String accessToken = jwtUtil.generateAccessToken(refreshToken);

        ResponseCookie refreshTokenCookie = CookieUtil.buildCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTtl);
        ResponseCookie accessTokenCookie = CookieUtil.buildCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTtl);

        log.info("Authentication successful. Access and refresh tokens are generated");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString(), accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        log.info("Attempt to refresh access token");
        if (refreshToken == null || authenticationService.tokenInBlackList(refreshToken)) {
            log.warn("Refresh token is null or invalidated : {}", refreshToken);
            throw new InvalidRefreshTokenException("Некорректный refresh токен");
        }

        String newAccessToken = jwtUtil.generateAccessToken(refreshToken);
        ResponseCookie accessTokenCookie = CookieUtil.buildCookie(ACCESS_TOKEN_COOKIE_NAME, newAccessToken, accessTtl);

        log.info("Access token successfully refreshed");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            log.info("Attempt to logout, invalidating refresh token");
            authenticationService.addTokenToBlackList(refreshToken, refreshTtl);
        }

        ResponseCookie refreshTokenCookie = CookieUtil.clearCookie(REFRESH_TOKEN_COOKIE_NAME);
        ResponseCookie accessTokenCookie = CookieUtil.clearCookie(ACCESS_TOKEN_COOKIE_NAME);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }
}
