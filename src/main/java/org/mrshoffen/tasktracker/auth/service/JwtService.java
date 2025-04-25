package org.mrshoffen.tasktracker.auth.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;
import org.mrshoffen.tasktracker.auth.jwt.deserializer.TokenDeserializer;
import org.mrshoffen.tasktracker.auth.jwt.factory.TokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.serializer.TokenSerializer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
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
        JwtToken deserializedRefreshToken = refreshTokenDeserializer.deserialize(jwtRefreshToken);

        if (refreshTokenInBlackList(deserializedRefreshToken)) {
            throw new InvalidRefreshTokenException("Refresh токен не действителен!");
        }

        if (deserializedRefreshToken.expiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Срок действия refresh токена истёк");
        }

        JwtToken accessToken = accessTokenFactory.generateToken(deserializedRefreshToken);
        return accessTokenSerializer.serialize(accessToken);
    }


    public void addRefreshTokenToBlackList(String refreshToken) {
        JwtToken deserializedRefreshToken = refreshTokenDeserializer.deserialize(refreshToken);
        addRefreshTokenToBlackList(deserializedRefreshToken);
    }

    private void addRefreshTokenToBlackList(JwtToken refreshToken) {
        jdbcTemplate.update("""
                INSERT INTO invalidated_tokens (id, c_keep_until) values (?, ?)
                """, refreshToken.id(), Date.from(refreshToken.expiresAt()));
        log.info("Jwt {} added to blacklist!", refreshToken);
    }

    private boolean refreshTokenInBlackList(JwtToken refreshToken) {
        return jdbcTemplate.queryForObject("""
                        SELECT EXISTS(SELECT id FROM invalidated_tokens WHERE id = ?)
                        """,
                Boolean.class,
                refreshToken.id());
    }

}
