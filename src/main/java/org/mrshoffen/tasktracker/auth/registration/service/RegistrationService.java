package org.mrshoffen.tasktracker.auth.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.exception.UnconfirmedRegistrationException;
import org.mrshoffen.tasktracker.auth.client.UserProfileClient;
import org.mrshoffen.tasktracker.auth.event.AuthEventPublisher;
import org.mrshoffen.tasktracker.auth.mapper.RegistrationMapper;
import org.mrshoffen.tasktracker.auth.registration.dto.RegistrationRequestDto;
import org.mrshoffen.tasktracker.auth.registration.exception.UserAlreadyExistsException;
import org.mrshoffen.tasktracker.auth.registration.repository.UnconfirmedRegistrationRepository;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    @Value("${app.registration.max-confirmation-time}")
    private Duration maxConfirmationTime;

    @Value("${app.registration.confirmation-link-prefix}")
    private String confirmationLinkPrefix;

    private final UserProfileClient userProfileClient;


    private final AuthEventPublisher authEventPublisher;

    private final PasswordEncoder passwordEncoder;

    private final UnconfirmedRegistrationRepository unconfirmedRegistrationRepository;

    private final RegistrationMapper registrationMapper;

    public String startUserRegistration(RegistrationRequestDto registrationDto) {
        if (unconfirmedRegistrationRepository.emailUnconfirmed(registrationDto.email())) {
            throw new UnconfirmedRegistrationException("Email уже использован, но не подтвержден. Пройдите по ссылке из письма");
        }

        if (userProfileClient.userExists(registrationDto.email())) {
            throw new UserAlreadyExistsException("Пользователь %s уже зарегистрирован!"
                    .formatted(registrationDto.email()));
        }


        RegistrationAttemptEvent event = buildRegistrationAttemptEvent(registrationDto);
        authEventPublisher.publishNewRegistrationEvent(event);

        unconfirmedRegistrationRepository.save(event, maxConfirmationTime);

        return event.getConfirmationLink(); //Сделано для упрощения тестирования и регистрации, чтобы не переходить в почту)
    }

    public void confirmUserRegistration(String registrationId) {
        RegistrationAttemptEvent registrationAttempt = unconfirmedRegistrationRepository.findById(registrationId)
                .orElseThrow(() ->
                        new UnconfirmedRegistrationException("Некорректная ссылка для подтверждения почты"));

        unconfirmedRegistrationRepository.delete(registrationAttempt);

        var successfulRegistration = registrationMapper.buildSuccessfulRegistrationEvent(registrationAttempt);
        authEventPublisher.publishSuccessfulRegistrationEvent(successfulRegistration);
    }


    private RegistrationAttemptEvent buildRegistrationAttemptEvent(RegistrationRequestDto registrationDto) {
        UUID registrationId = UUID.randomUUID();
        return RegistrationAttemptEvent.builder()
                .registrationId(registrationId)
                .confirmationLink(confirmationLinkPrefix.formatted(registrationId.toString()))
                .email(registrationDto.email())
                .hashedPassword(passwordEncoder.encode(registrationDto.password()))
                .avatarUrl(registrationDto.avatarUrl())
                .firstName(registrationDto.firstName())
                .lastName(registrationDto.lastName())
                .validUntil(Instant.now().plus(maxConfirmationTime))
                .build();
    }
}
