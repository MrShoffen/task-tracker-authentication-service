package org.mrshoffen.tasktracker.auth.registration.dto;


import org.mrshoffen.tasktracker.auth.util.validation.ValidEmail;
import org.mrshoffen.tasktracker.auth.util.validation.ValidPassword;

public record RegistrationRequestDto(
        @ValidEmail
        String email,

        @ValidPassword
        String password,

        String avatarUrl, //todo add validation

        String firstName,

        String lastName
) {
}
