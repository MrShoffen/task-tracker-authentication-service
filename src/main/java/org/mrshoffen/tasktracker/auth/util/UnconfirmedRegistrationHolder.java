package org.mrshoffen.tasktracker.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

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

    public Optional<RegistrationAttemptEvent> findRegistrationAttempt(String registrationId) {
        String json = redisTemplate.opsForValue().get("unconfirmed:" + registrationId);
        try {
            RegistrationAttemptEvent attempt = objectMapper.readValue(json, RegistrationAttemptEvent.class);
            return Optional.of(attempt);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void deleteRegistrationAttempt(RegistrationAttemptEvent attempt) {
        redisTemplate.delete("unconfirmed:" + attempt.getRegistrationId());
        redisTemplate.delete(attempt.getEmail());
    }

    public boolean emailUnconfirmed(String email) {
        String registration = redisTemplate.opsForValue().get(email);
        return registration != null;
    }
}
