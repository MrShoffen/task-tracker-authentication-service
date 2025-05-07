package org.mrshoffen.tasktracker.auth.registration.repository;

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
public class UnconfirmedRegistrationAttemptRepository {

    private static final String UNCONFIRMED_REG_PREFIX_KEY = "unconfirmed:";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void save(RegistrationAttemptEvent event, Duration saveDuration) {
        String json = objectMapper.writeValueAsString(event);
        redisTemplate.opsForValue().set(
                UNCONFIRMED_REG_PREFIX_KEY + event.getRegistrationId(),
                json,
                saveDuration
        );
        redisTemplate.opsForValue().set(UNCONFIRMED_REG_PREFIX_KEY + event.getEmail(), "", saveDuration);
    }

    public Optional<RegistrationAttemptEvent> findById(String registrationId) {
        String json = redisTemplate.opsForValue().get(UNCONFIRMED_REG_PREFIX_KEY + registrationId);
        try {
            RegistrationAttemptEvent attempt = objectMapper.readValue(json, RegistrationAttemptEvent.class);
            return Optional.of(attempt);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void delete(RegistrationAttemptEvent attempt) {
        redisTemplate.delete(UNCONFIRMED_REG_PREFIX_KEY + attempt.getRegistrationId());
        redisTemplate.delete(UNCONFIRMED_REG_PREFIX_KEY + attempt.getEmail());
    }

    public boolean emailUnconfirmed(String email) {
        String registration = redisTemplate.opsForValue().get(UNCONFIRMED_REG_PREFIX_KEY + email);
        return registration != null;
    }
}
