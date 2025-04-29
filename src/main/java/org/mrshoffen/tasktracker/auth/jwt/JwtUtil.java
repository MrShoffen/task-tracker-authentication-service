package org.mrshoffen.tasktracker.auth.jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.authentication.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.authentication.exception.RefreshTokenExpiredException;
import org.mrshoffen.tasktracker.auth.jwt.deserializer.TokenDeserializer;
import org.mrshoffen.tasktracker.auth.jwt.factory.TokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.serializer.TokenSerializer;

import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final TokenFactory<Map<String, String>> refreshTokenFactory;
    private final TokenFactory<JwtToken> accessTokenFactory;

    private final TokenSerializer refreshTokenSerializer;
    private final TokenSerializer accessTokenSerializer;

    private final TokenDeserializer refreshTokenDeserializer;

    public String generateRefreshToken(Map<String, String> payload) {
        JwtToken refreshToken = refreshTokenFactory.generateToken(payload);
        return refreshTokenSerializer.serialize(refreshToken);
    }

    public String generateAccessToken(String jwtRefreshToken) throws InvalidRefreshTokenException, RefreshTokenExpiredException {
        JwtToken deserializedRefreshToken = refreshTokenDeserializer.deserialize(jwtRefreshToken);

        if (deserializedRefreshToken.expiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Срок действия refresh токена истёк");
        }

        JwtToken accessToken = accessTokenFactory.generateToken(deserializedRefreshToken);
        return accessTokenSerializer.serialize(accessToken);
    }



}
