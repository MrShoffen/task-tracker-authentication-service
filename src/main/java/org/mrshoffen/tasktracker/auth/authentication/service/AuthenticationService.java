package org.mrshoffen.tasktracker.auth.authentication.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.dto.LoginDto;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.client.UserProfileClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationService {

    private final UserProfileClient userProfileClient;

    private final PasswordEncoder passwordEncoder;

    public void validateUserCredentials(String email, String password) {
        try {
            log.info("Attempt to authenticate - trying to validate user in user-profile-ws {}", email);
            String hashedPassword = userProfileClient.userHashedPassword(email);
            if (!passwordEncoder.matches(password, hashedPassword)) {
                log.warn("Password doesnt match");
                throw new InvalidCredentialsException("Неверный логин или пароль");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Failed to fetch user {} from user-profile-ws", email);
            throw new InvalidCredentialsException("Неверный логин или пароль", e);
        }
    }

    public String getUserId(String email) {
        try {
            return userProfileClient.userId(email);
        } catch (FeignException.NotFound ex) {
            throw new InvalidCredentialsException("Неверный логин или пароль");

        }
    }


}
