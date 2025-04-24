package org.mrshoffen.tasktracker.auth.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record JwtToken(UUID id,
                       Map<String, String> payload,
                       Instant createdAt,
                       Instant expiresAt) {
}