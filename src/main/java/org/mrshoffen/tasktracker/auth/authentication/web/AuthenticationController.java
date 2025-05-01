package org.mrshoffen.tasktracker.auth.authentication.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.dto.LoginDto;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.authentication.exception.UnconfirmedRegistrationException;
import org.mrshoffen.tasktracker.auth.authentication.service.AuthenticationService;
import org.mrshoffen.tasktracker.auth.event.AuthEventPublisher;
import org.mrshoffen.tasktracker.auth.util.jwt.BlacklistTokenHolder;
import org.mrshoffen.tasktracker.auth.util.jwt.JwtUtil;
import org.mrshoffen.tasktracker.auth.util.CookieBuilderUtil;
import org.mrshoffen.tasktracker.auth.util.UnconfirmedRegistrationHolder;
import org.mrshoffen.tasktracker.commons.kafka.event.authentication.AuthenticationSuccessfulEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME;
import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final JwtUtil jwtUtil;

    private final AuthenticationService authenticationService;

    private final UnconfirmedRegistrationHolder unconfirmedRegistrationHolder;

    private final BlacklistTokenHolder blacklistTokenHolder;

    private final AuthEventPublisher eventPublisher;

    @Value("${jwt-user.ttl.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${jwt-user.ttl.access-ttl}")
    private Duration accessTtl;

    @PostMapping("/sign-in")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginDto loginDto,
                                      @RequestHeader(value = "X-Forwarded-For", required = false) String userIp) {
        if (unconfirmedRegistrationHolder.emailUnconfirmed(loginDto.email())) {
            throw new UnconfirmedRegistrationException("Данный email не подтвержден. Пройдите по ссылке, которую прислали в ссылке");
        }

        authenticationService.validateUserCredentials(loginDto);
        String userId = authenticationService.getUserId(loginDto.email());

        String refreshToken = jwtUtil.generateRefreshToken(Map.of("userId", userId, "userEmail", loginDto.email()));
        String accessToken = jwtUtil.generateAccessToken(refreshToken);

        ResponseCookie refreshTokenCookie = CookieBuilderUtil.withNameAndValue(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTtl);
        ResponseCookie accessTokenCookie = CookieBuilderUtil.withNameAndValue(ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTtl);

        log.info("Authentication successful. Access and refresh tokens are generated");

        var authenticationSuccessfulEvent = new AuthenticationSuccessfulEvent(UUID.fromString(userId), loginDto.email(), userIp);
        eventPublisher.publishSuccessfulAuthenticationEvent(authenticationSuccessfulEvent);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString(), accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        log.info("Attempt to refresh access token");
        if (refreshToken == null || blacklistTokenHolder.tokenInBlackList(refreshToken)) {
            log.warn("Refresh token is null or invalidated : {}", refreshToken);
            throw new InvalidRefreshTokenException("Некорректный refresh токен");
        }

        String newAccessToken = jwtUtil.generateAccessToken(refreshToken);
        ResponseCookie accessTokenCookie = CookieBuilderUtil.withNameAndValue(ACCESS_TOKEN_COOKIE_NAME, newAccessToken, accessTtl);

        log.info("Access token successfully refreshed");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            log.info("Attempt to logout, invalidating refresh token");
            blacklistTokenHolder.addTokenToBlackList(refreshToken, refreshTtl);
        }

        ResponseCookie refreshTokenCookie = CookieBuilderUtil.clear(REFRESH_TOKEN_COOKIE_NAME);
        ResponseCookie accessTokenCookie = CookieBuilderUtil.clear(ACCESS_TOKEN_COOKIE_NAME);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }
}
