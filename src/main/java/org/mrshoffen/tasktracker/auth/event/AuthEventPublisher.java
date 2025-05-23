package org.mrshoffen.tasktracker.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.commons.kafka.event.authentication.AuthenticationSuccessfulEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.EmailUpdateAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.EmailUpdatedSuccessEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.PasswordUpdatedEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationSuccessfulEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final KafkaTemplate<UUID, Object> kafkaTemplate;


    public void publishNewRegistrationEvent(RegistrationAttemptEvent event) {
        kafkaTemplate.send(RegistrationAttemptEvent.TOPIC, event.getRegistrationId(), event);
        log.info("Event published to kafka topic '{}' - {}", RegistrationAttemptEvent.TOPIC, event);
    }

    public void publishSuccessfulRegistrationEvent(RegistrationSuccessfulEvent event) {
        kafkaTemplate.send(RegistrationSuccessfulEvent.TOPIC, event.getRegistrationId(), event);
        log.info("Event published to kafka topic '{}' - {}", RegistrationSuccessfulEvent.TOPIC, event);

    }

    public void publishSuccessfulAuthenticationEvent(AuthenticationSuccessfulEvent event) {
        kafkaTemplate.send(AuthenticationSuccessfulEvent.TOPIC, event.getUserId(), event);
        log.info("Event published to kafka topic '{}' - {}", AuthenticationSuccessfulEvent.TOPIC, event);
    }

    public void publishPasswordUpdatedEvent(PasswordUpdatedEvent event) {
        kafkaTemplate.send(PasswordUpdatedEvent.TOPIC, event.getUserId(), event);
        log.info("Event published to kafka topic '{}' - {}", PasswordUpdatedEvent.TOPIC, event);

    }

    public void publishEmailUpdateAttempt(EmailUpdateAttemptEvent event) {
        kafkaTemplate.send(EmailUpdateAttemptEvent.TOPIC, event.getUserId(), event);
        log.info("Event published to kafka topic '{}' - {}", EmailUpdateAttemptEvent.TOPIC, event);
    }

    public void publishSuccessfulEmailUpdateEvent(EmailUpdatedSuccessEvent event) {
        kafkaTemplate.send(EmailUpdatedSuccessEvent.TOPIC, event.getUserId(), event);
        log.info("Event published to kafka topic '{}' - {}", EmailUpdatedSuccessEvent.TOPIC, event);

    }
}
