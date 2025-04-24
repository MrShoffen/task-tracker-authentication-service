package org.mrshoffen.tasktracker.auth.jwt.factory;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class RefreshJweTokenFactory implements TokenFactory<Map<String, String>> {

    private final Duration ttl;

    @Override
    public JwtToken generateToken(Map<String, String> payload) {
        Instant now = Instant.now();
        return new JwtToken(UUID.randomUUID(), payload, now, now.plus(ttl));
    }
}
