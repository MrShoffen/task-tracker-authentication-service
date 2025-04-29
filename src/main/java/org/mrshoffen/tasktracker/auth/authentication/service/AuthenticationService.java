package org.mrshoffen.tasktracker.auth.authentication.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.dto.LoginDto;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.util.client.UserProfileClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationService {

    private final UserProfileClient userProfileClient;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    public void validateCredentialsAndGetUserId(LoginDto loginDto) {
        try {
            log.info("Attempt to authenticate - trying to validate user in user-profile-ws {}", loginDto);
            String hashedPassword = userProfileClient.userHashedPassword(loginDto.email());
            if (!passwordEncoder.matches(loginDto.password(), hashedPassword)) {
                log.warn("Password didnt match");
                throw new InvalidCredentialsException("Неверный логин или пароль");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Failed to fetch user {} from user-profile-ws", loginDto.email());
            throw new InvalidCredentialsException("Неверный логин или пароль", e);
        }
    }

    public String getUserId(String email){
        return userProfileClient.userId(email);
    }

    public void addTokenToBlackList(String token, Duration ttl) {
        redisTemplate.opsForValue()
                .set("blacklist:" + token,
                        "logged_out",
                        ttl);
    }

    public boolean tokenInBlackList(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

}
