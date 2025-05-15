package org.mrshoffen.tasktracker.auth.credentials.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.credentials.dto.EmailUpdateDto;
import org.mrshoffen.tasktracker.auth.credentials.dto.PasswordUpdateDto;
import org.mrshoffen.tasktracker.auth.credentials.service.CredentiaslService;
import org.mrshoffen.tasktracker.auth.authentication.service.AuthenticationService;
import org.mrshoffen.tasktracker.auth.registration.dto.RegistrationRequestDto;
import org.mrshoffen.tasktracker.auth.util.CookieBuilderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/credentials")
public class CredentialsController {

    private final CredentiaslService credentiaslService;

    private final AuthenticationService authenticationService;

    @PatchMapping("/password")
    ResponseEntity<Void> updatePassword(@RequestHeader(AUTHORIZED_USER_HEADER_NAME) UUID userId,
                                        @Valid @RequestBody PasswordUpdateDto passwordUpdateDto) {
        String userEmail = credentiaslService.getUserEmail(userId);

        authenticationService
                .validateUserCredentials(userEmail, passwordUpdateDto.oldPassword());

        credentiaslService.updatePassword(userId, passwordUpdateDto.newPassword());

        ResponseCookie refreshTokenCookie = CookieBuilderUtil.clear(REFRESH_TOKEN_COOKIE_NAME);
        ResponseCookie accessTokenCookie = CookieBuilderUtil.clear(ACCESS_TOKEN_COOKIE_NAME);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }

    @PatchMapping("/email")
    ResponseEntity<Map<String, String>> startUserRegistration(@RequestHeader(AUTHORIZED_USER_HEADER_NAME) UUID userId,
                                                              @Valid @RequestBody EmailUpdateDto emailUpdateDto) {

        credentiaslService.startEmailUpdate(userId, emailUpdateDto.newEmail());
        return ResponseEntity.accepted()
                .body(Map.of(
                        "message", "На Вашу почту %s отправлено письмо с кодом для подтверждения новой почты.".formatted(emailUpdateDto.newEmail())
                ));
    }

    @GetMapping("/email")
    ResponseEntity<Map<String, String>> confirmUserRegistration(@RequestHeader(AUTHORIZED_USER_HEADER_NAME) UUID userId,
                                                                @RequestParam("confirm") String confirmationCode) {

        credentiaslService.confirmEmail(userId, confirmationCode);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Почта подтверждена!"));
    }
}
