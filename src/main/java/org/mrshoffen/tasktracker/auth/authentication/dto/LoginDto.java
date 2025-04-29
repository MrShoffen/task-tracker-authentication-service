package org.mrshoffen.tasktracker.auth.authentication.dto;

import org.mrshoffen.tasktracker.auth.util.validation.ValidEmail;
import org.mrshoffen.tasktracker.auth.util.validation.ValidPassword;

public record LoginDto(
        @ValidEmail
        String email,
        
        @ValidPassword
        String password) {
}
