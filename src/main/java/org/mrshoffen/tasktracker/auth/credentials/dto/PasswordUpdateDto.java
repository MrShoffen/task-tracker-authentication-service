package org.mrshoffen.tasktracker.auth.credentials.dto;

import org.mrshoffen.tasktracker.auth.util.validation.ValidPassword;

public record PasswordUpdateDto(
        @ValidPassword
        String oldPassword,

        @ValidPassword
        String newPassword
) {
}
