package org.mrshoffen.tasktracker.auth.util.jwt.factory;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.util.jwt.JwtToken;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public class AccessJwsTokenFactory implements TokenFactory<JwtToken> {

    private final Duration ttl;

    @Override
    public JwtToken generateToken(JwtToken refreshToken) {
        Instant now = Instant.now();
        return new JwtToken(refreshToken.id(), refreshToken.payload(), now, now.plus(ttl));
    }
}
