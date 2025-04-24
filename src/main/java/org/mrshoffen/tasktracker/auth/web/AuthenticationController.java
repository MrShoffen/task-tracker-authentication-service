package org.mrshoffen.tasktracker.auth.web;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.LoginDto;
import org.mrshoffen.tasktracker.auth.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.service.AuthenticationService;
import org.mrshoffen.tasktracker.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final JwtService jwtService;

    @Value("${jwt-user.ttl.refresh-ttl}")
    private int refreshTtl;

    @Value("${jwt-user.ttl.access-ttl}")
    private int accessTtl;


    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginDto loginDto) {
        String userId = authenticationService.validateAndGetUserId(loginDto);

        String refreshToken = jwtService.generateRefreshToken(Map.of("userId", userId, "userEmail", loginDto.email()));
        String accessToken = jwtService.generateAccessToken(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .path("/")
                .maxAge(Duration.ofHours(refreshTtl))
                .httpOnly(true)
                .build(); //todo samesite?

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", accessToken)
                .path("/")
                .maxAge(Duration.ofMinutes(accessTtl))
                .httpOnly(true)
                .build();


        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString(), accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        String newAccessToken = jwtService.generateAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", newAccessToken)
                .path("/")
                .maxAge(Duration.ofMinutes(accessTtl))
                .httpOnly(true)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", "")
                .maxAge(0)
                .httpOnly(true)
                .build();

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", "")
                .maxAge(0)
                .httpOnly(true)
                .build();

        jwtService.invalidateRefreshToken(refreshToken);


        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }


    @ExceptionHandler({InvalidCredentialsException.class,
            InvalidRefreshTokenException.class,
            RefreshTokenExpiredException.class})
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }

}
