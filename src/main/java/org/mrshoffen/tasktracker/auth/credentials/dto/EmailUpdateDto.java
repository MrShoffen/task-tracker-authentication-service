package org.mrshoffen.tasktracker.auth.credentials.dto;

import org.mrshoffen.tasktracker.auth.util.validation.ValidEmail;

public record EmailUpdateDto(
        @ValidEmail
        String newEmail
) {
}
