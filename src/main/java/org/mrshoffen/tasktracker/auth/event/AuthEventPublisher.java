package org.mrshoffen.tasktracker.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.commons.kafka.event.auth.RegistrationAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.auth.RegistrationSuccessfulEvent;
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
        kafkaTemplate.send(RegistrationSuccessfulEvent.TOPIC, event.getUserId(), event);
        log.info("Event published to kafka topic '{}' - {}", RegistrationSuccessfulEvent.TOPIC, event);

    }

}
