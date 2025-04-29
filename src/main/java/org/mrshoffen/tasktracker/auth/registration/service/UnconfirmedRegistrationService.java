package org.mrshoffen.tasktracker.auth.registration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.registration.exception.EmailUnconfirmedException;
import org.mrshoffen.tasktracker.commons.kafka.event.auth.RegistrationAttemptEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
@Slf4j
public class UnconfirmedRegistrationService {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void saveRegistrationAttempt(RegistrationAttemptEvent event) {
        String json = objectMapper.writeValueAsString(event);
        redisTemplate.opsForValue().set(
                "unconfirmed:" + event.getRegistrationId(),
                json,
                Duration.ofMinutes(10)
        );
    }

    public RegistrationAttemptEvent findRegistrationAttempt(String registrationId) {
        String json = redisTemplate.opsForValue().get("unconfirmed:" + registrationId);

        try {
            return objectMapper.readValue(json, RegistrationAttemptEvent.class);
        } catch (Exception e) {
            throw new EmailUnconfirmedException("Не удалось подтвердить почту", e);
        }
    }

    public void deleteRegistrationAttempt(String registrationId) {
        redisTemplate.delete("unconfirmed:" + registrationId);
    }
}
