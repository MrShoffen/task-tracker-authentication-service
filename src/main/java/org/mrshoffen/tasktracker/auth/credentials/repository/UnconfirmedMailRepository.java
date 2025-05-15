package org.mrshoffen.tasktracker.auth.credentials.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.commons.kafka.event.creds.EmailUpdateAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class UnconfirmedMailRepository {

    private static final String UNCONFIRMED_REG_PREFIX_KEY = "unconfirmed:";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void save(EmailUpdateAttemptEvent event, Duration saveDuration) {
        String json = objectMapper.writeValueAsString(event);
        redisTemplate.opsForValue().set(
                UNCONFIRMED_REG_PREFIX_KEY + event.getUserId(),
                json,
                saveDuration
        );
        redisTemplate.opsForValue().set(UNCONFIRMED_REG_PREFIX_KEY + event.getNewEmail().toLowerCase(), "", saveDuration);
    }


    public Optional<EmailUpdateAttemptEvent> findById(UUID id) {
        String json = redisTemplate.opsForValue().get(UNCONFIRMED_REG_PREFIX_KEY + id.toString());
        try {
            EmailUpdateAttemptEvent attempt = objectMapper.readValue(json, EmailUpdateAttemptEvent.class);
            return Optional.of(attempt);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void delete(EmailUpdateAttemptEvent event) {
        redisTemplate.delete(UNCONFIRMED_REG_PREFIX_KEY + event.getUserId());
        redisTemplate.delete(UNCONFIRMED_REG_PREFIX_KEY + event.getNewEmail());
    }

    public boolean emailUnconfirmed(String email) {
        String registration = redisTemplate.opsForValue().get(UNCONFIRMED_REG_PREFIX_KEY + email.toLowerCase());
        return registration != null;
    }
}
