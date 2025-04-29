package org.mrshoffen.tasktracker.auth.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.event.AuthEventPublisher;
import org.mrshoffen.tasktracker.auth.registration.dto.RegistrationRequestDto;
import org.mrshoffen.tasktracker.auth.registration.exception.UserAlreadyExistsException;
import org.mrshoffen.tasktracker.auth.util.client.IpApiClient;
import org.mrshoffen.tasktracker.auth.util.client.UserProfileClient;
import org.mrshoffen.tasktracker.commons.kafka.event.auth.RegistrationAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.auth.RegistrationSuccessfulEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserProfileClient userProfileClient;

    private final IpApiClient ipApiClient;

    private final AuthEventPublisher authEventPublisher;

    private final PasswordEncoder passwordEncoder;

    private final UnconfirmedRegistrationService unconfirmedRegistrationService;

    public void startUserRegistration(RegistrationRequestDto registrationDto, String ipAddr) {
        if (userProfileClient.userExists(registrationDto.email())) {
            throw new UserAlreadyExistsException("Пользователь %s уже зарегистрирован!"
                    .formatted(registrationDto.email()));
        }

        IpApiClient.IpInfo ipInfo = ipApiClient.getIpInfo(ipAddr);

        RegistrationAttemptEvent event = RegistrationAttemptEvent.builder()
                .registrationId(UUID.randomUUID())
                .email(registrationDto.email())
                .hashedPassword(passwordEncoder.encode(registrationDto.password()))
                .timeZone(ipInfo.getTimeZone())
                .country(ipInfo.getCountry())
                .region(ipInfo.getRegion())
                .build();

        unconfirmedRegistrationService.saveRegistrationAttempt(event);

        authEventPublisher.publishNewRegistrationEvent(event);
    }

    public void confirmUserRegistration(String registrationId) {
        RegistrationAttemptEvent registrationAttempt = unconfirmedRegistrationService.findRegistrationAttempt(registrationId);
        unconfirmedRegistrationService.deleteRegistrationAttempt(registrationId);

        RegistrationSuccessfulEvent successfulRegistration = RegistrationSuccessfulEvent.builder()
                .userId(UUID.randomUUID())
                .email(registrationAttempt.getEmail())
                .hashedPassword(registrationAttempt.getHashedPassword())
                .timeZone(registrationAttempt.getTimeZone())
                .country(registrationAttempt.getCountry())
                .region(registrationAttempt.getRegion())
                .build();

        authEventPublisher.publishSuccessfulRegistrationEvent(successfulRegistration);
    }
}
