package org.mrshoffen.tasktracker.auth.registration.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.exception.UnconfirmedRegistrationException;
import org.mrshoffen.tasktracker.auth.event.AuthEventPublisher;
import org.mrshoffen.tasktracker.auth.registration.dto.RegistrationRequestDto;
import org.mrshoffen.tasktracker.auth.registration.exception.UserAlreadyExistsException;
import org.mrshoffen.tasktracker.auth.registration.repository.UnconfirmedRegistrationAttemptRepository;
import org.mrshoffen.tasktracker.auth.util.client.IpApiClient;
import org.mrshoffen.tasktracker.auth.util.client.UserProfileClient;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationSuccessfulEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    @Value("${registration.max-confirmation-time}")
    private Duration maxConfirmationTime;

    @Value("${registration.confirmation-link-prefix}")
    private String confirmationLinkPrefix;

    private final UserProfileClient userProfileClient;

    private final IpApiClient ipApiClient;

    private final AuthEventPublisher authEventPublisher;

    private final PasswordEncoder passwordEncoder;

    private final UnconfirmedRegistrationAttemptRepository unconfirmedRegRepo;

    public void startUserRegistration(RegistrationRequestDto registrationDto, String ipAddr) {
        if (unconfirmedRegRepo.emailUnconfirmed(registrationDto.email())) {
            throw new UnconfirmedRegistrationException("Email уже использован, но не подтвержден. Пройдите по ссылке из письма");
        }

        if (userProfileClient.userExists(registrationDto.email())) {
            throw new UserAlreadyExistsException("Пользователь %s уже зарегистрирован!"
                    .formatted(registrationDto.email()));
        }

        IpApiClient.IpInfo ipInfo = ipApiClient.getIpInfo(ipAddr);

        RegistrationAttemptEvent event = buildRegistrationAttemptEvent(registrationDto, ipInfo);
        authEventPublisher.publishNewRegistrationEvent(event);

        unconfirmedRegRepo.save(event, maxConfirmationTime);
    }

    public void confirmUserRegistration(String registrationId) {
        RegistrationAttemptEvent registrationAttempt = unconfirmedRegRepo.findById(registrationId)
                .orElseThrow(() ->
                        new UnconfirmedRegistrationException("Некорректная ссылка для подтверждения почты"));

        unconfirmedRegRepo.delete(registrationAttempt);

        var successfulRegistration = new RegistrationSuccessfulEvent(registrationAttempt.getRegistrationId(), registrationAttempt.getEmail());
        authEventPublisher.publishSuccessfulRegistrationEvent(successfulRegistration);
    }

    private RegistrationAttemptEvent buildRegistrationAttemptEvent(RegistrationRequestDto registrationDto, IpApiClient.IpInfo ipInfo) {
        UUID registrationId = UUID.randomUUID();
        return RegistrationAttemptEvent.builder()
                .registrationId(registrationId)
                .confirmationLink(confirmationLinkPrefix.formatted(registrationId.toString()))
                .email(registrationDto.email())
                .hashedPassword(passwordEncoder.encode(registrationDto.password()))
                .avatarUrl(registrationDto.avatarUrl())
                .firstName(registrationDto.firstName())
                .lastName(registrationDto.lastName())
                .timeZone(ipInfo.getTimeZone())
                .country(ipInfo.getCountry())
                .region(ipInfo.getRegion())
                .validUntil(Instant.now().plus(maxConfirmationTime))
                .build();
    }
}
