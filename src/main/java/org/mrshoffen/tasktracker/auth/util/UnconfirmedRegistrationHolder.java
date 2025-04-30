package org.mrshoffen.tasktracker.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.registration.exception.EmailUnconfirmedException;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@AllArgsConstructor
@Slf4j
public class UnconfirmedRegistrationHolder {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void saveRegistrationAttempt(RegistrationAttemptEvent event, Duration saveDuration) {
        String json = objectMapper.writeValueAsString(event);
        redisTemplate.opsForValue().set(
                "unconfirmed:" + event.getRegistrationId(),
                json,
                saveDuration
        );
        redisTemplate.opsForValue().set(event.getEmail(), "unconfirmed", saveDuration);
    }

    public RegistrationAttemptEvent findRegistrationAttempt(String registrationId) {
        String json = redisTemplate.opsForValue().get("unconfirmed:" + registrationId);
        try {
            RegistrationAttemptEvent attempt = objectMapper.readValue(json, RegistrationAttemptEvent.class);
            redisTemplate.delete(attempt.getEmail());
            return attempt;
        } catch (Exception e) {
            throw new EmailUnconfirmedException("Не удалось подтвердить почту - некорректная ссылка подтверждения", e);
        }
    }

    public void deleteRegistrationAttempt(String registrationId) {
        redisTemplate.delete("unconfirmed:" + registrationId);
    }

    public boolean registrationInProgress(String email) {
        String registration = redisTemplate.opsForValue().get(email);
        return registration != null;
    }
}
