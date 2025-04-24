package org.mrshoffen.tasktracker.auth.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.LoginDto;
import org.mrshoffen.tasktracker.auth.UserProfileClient;
import org.mrshoffen.tasktracker.auth.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserProfileClient userProfileClient;

    public String validateAndGetUserId(LoginDto loginDto) {
        try {
            return userProfileClient.validateUser(loginDto.email(), loginDto.password());
        } catch (FeignException.NotFound e) {
            throw new InvalidCredentialsException("Неверный логин или пароль", e);
        }

    }

}
