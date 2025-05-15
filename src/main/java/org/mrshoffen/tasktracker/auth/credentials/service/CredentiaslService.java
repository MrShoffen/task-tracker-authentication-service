package org.mrshoffen.tasktracker.auth.credentials.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidCredentialsException;
import org.mrshoffen.tasktracker.auth.authentication.exception.UnconfirmedRegistrationException;
import org.mrshoffen.tasktracker.auth.client.UserProfileClient;
import org.mrshoffen.tasktracker.auth.credentials.repository.UnconfirmedMailRepository;
import org.mrshoffen.tasktracker.auth.event.AuthEventPublisher;
import org.mrshoffen.tasktracker.auth.registration.exception.UserAlreadyExistsException;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.EmailUpdateAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.EmailUpdatedSuccessEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.PasswordUpdatedEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredentiaslService {

    private final UserProfileClient userProfileClient;

    private final PasswordEncoder passwordEncoder;

    private final AuthEventPublisher eventPublisher;

    private final UnconfirmedMailRepository unconfirmedMailRepository;

    @Value("${app.registration.max-confirmation-time}")
    private Duration maxConfirmationTime;


    public String getUserEmail(UUID userId) {
        try {
            return userProfileClient.email(userId);
        } catch (FeignException.NotFound ex) {

            throw new InvalidCredentialsException("Пользователь с id не найден");
        }
    }

    public void updatePassword(UUID userId, String password) {
        String hashedNewPass = passwordEncoder.encode(password);

        PasswordUpdatedEvent passwordUpdatedEvent = new PasswordUpdatedEvent(userId, hashedNewPass);

        eventPublisher.publishPasswordUpdatedEvent(passwordUpdatedEvent);
    }

    public void startEmailUpdate(UUID userId, String newEmail) {
        if (unconfirmedMailRepository.emailUnconfirmed(newEmail)) {
            throw new UnconfirmedRegistrationException("Email уже в процессе обновления, но не подтвержден. Пройдите по ссылке из письма");
        }

        if (userProfileClient.userExists(newEmail)) {
            throw new UserAlreadyExistsException("Данный email уже занят!");
        }

        String confirmationCode = generateConfirmationCode();

        EmailUpdateAttemptEvent event = new EmailUpdateAttemptEvent(userId, newEmail, confirmationCode);
        eventPublisher.publishEmailUpdateAttempt(event);

        unconfirmedMailRepository.save(event, maxConfirmationTime);
    }

    private String generateConfirmationCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(10); // Генерация цифры от 0 до 9
            sb.append(digit);
        }

        return sb.toString();
    }

    public void confirmEmail(UUID userId, String confirmationCode) {
        EmailUpdateAttemptEvent emailUpdate = unconfirmedMailRepository.findById(userId)
                .filter(emailUpdateEvent -> emailUpdateEvent.getConfirmationCode().equals(confirmationCode))
                .orElseThrow(() ->
                        new UnconfirmedRegistrationException("Неверный код подтверждения"));

        unconfirmedMailRepository.delete(emailUpdate);

        EmailUpdatedSuccessEvent successfulUpdate = new EmailUpdatedSuccessEvent(userId, emailUpdate.getNewEmail());
        eventPublisher.publishSuccessfulEmailUpdateEvent(successfulUpdate);
    }
}
