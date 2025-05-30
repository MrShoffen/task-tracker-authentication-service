package org.mrshoffen.tasktracker.auth.registration.web;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.registration.dto.RegistrationRequestDto;
import org.mrshoffen.tasktracker.auth.registration.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/sign-up")
    ResponseEntity<Map<String, String>> startUserRegistration(@Valid @RequestBody RegistrationRequestDto registrationDto) {

        String confirmLink = registrationService.startUserRegistration(registrationDto);
        return ResponseEntity.accepted()
                .body(Map.of(
                        "message", "На Вашу почту %s отправлено письмо с подтверждением регистрации.".formatted(registrationDto.email()),
                        "link", confirmLink
                ));
    }

    @GetMapping("/sign-up")
    ResponseEntity<Map<String, String>> confirmUserRegistration(@RequestParam("confirm") String registrationId) {

        registrationService.confirmUserRegistration(registrationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Аккаунт успешно создан!"));
    }
}
