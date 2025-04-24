package org.mrshoffen.tasktracker.auth.service;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;
import org.mrshoffen.tasktracker.auth.jwt.deserializer.TokenDeserializer;
import org.mrshoffen.tasktracker.auth.jwt.factory.AccessJwsTokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.factory.RefreshJweTokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.factory.TokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.serializer.TokenSerializer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
public class JwtService {

    private final TokenFactory<Map<String, String>> refreshTokenFactory;
    private final TokenFactory<JwtToken> accessTokenFactory;

    private final TokenSerializer refreshTokenSerializer;
    private final TokenSerializer accessTokenSerializer;

    private final TokenDeserializer refreshTokenDeserializer;

    private final JdbcTemplate jdbcTemplate;

    public String generateRefreshToken(Map<String, String> payload) {
        JwtToken refreshToken = refreshTokenFactory.generateToken(payload);
        return refreshTokenSerializer.serialize(refreshToken);
    }

    public String generateAccessToken(String jwtRefreshToken) {
        JwtToken deserRefreshToken = refreshTokenDeserializer.deserialize(jwtRefreshToken);

        if (refreshTokenInvalidated(deserRefreshToken)) {
            throw new InvalidRefreshTokenException("Refresh токен не действителен!");
        }

        if (deserRefreshToken.expiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Срок действия refresh токена истёк");
        }

        JwtToken accessToken = accessTokenFactory.generateToken(deserRefreshToken);
        return accessTokenSerializer.serialize(accessToken);
    }


    public void invalidateRefreshToken(String refreshToken) {
        JwtToken deserRefresh = refreshTokenDeserializer.deserialize(refreshToken);
        invalidateRefreshToken(deserRefresh);
    }

    private void invalidateRefreshToken(JwtToken refreshToken) {
        jdbcTemplate.update("""
                INSERT INTO invalidated_tokens (id, c_keep_until) values (?, ?)
                """, refreshToken.id(), Date.from(refreshToken.expiresAt()));
    }

    private boolean refreshTokenInvalidated(JwtToken refreshToken) {

        return jdbcTemplate.queryForObject("""
                        SELECT EXISTS(SELECT id FROM invalidated_tokens WHERE id = ?)
                        """,
                Boolean.class,
                refreshToken.id());
    }

}
