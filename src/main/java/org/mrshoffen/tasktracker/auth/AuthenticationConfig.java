package org.mrshoffen.tasktracker.auth;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.auth.jwt.deserializer.RefreshJweTokenDeserializer;
import org.mrshoffen.tasktracker.auth.jwt.factory.AccessJwsTokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.factory.RefreshJweTokenFactory;
import org.mrshoffen.tasktracker.auth.jwt.serializer.AccessJwsTokenSerializer;
import org.mrshoffen.tasktracker.auth.jwt.serializer.RefreshJweTokenSerializer;
import org.mrshoffen.tasktracker.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.ParseException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final JdbcTemplate jdbcTemplate;


    @Bean
    JwtService jwtService(@Value("${jwt-user.keys.refresh-token-key}") String refreshKey,
                          @Value("${jwt-user.keys.access-token-key}") String accessKey,
                          @Value("${jwt-user.ttl.refresh-ttl}") Duration refreshTtl,
                          @Value("${jwt-user.ttl.access-ttl}") Duration accessTtl) throws ParseException, JOSEException {

        var refreshTokenFactory = new RefreshJweTokenFactory(refreshTtl);
        var accessTokenFactory = new AccessJwsTokenFactory(accessTtl);

        var refreshTokenSerializer = new RefreshJweTokenSerializer(
                new DirectEncrypter(OctetSequenceKey.parse(refreshKey)),
                JWEAlgorithm.DIR,
                EncryptionMethod.A192GCM
        );
        var refreshTokenDeserializer = new RefreshJweTokenDeserializer(new DirectDecrypter(OctetSequenceKey.parse(refreshKey)));

        var accessTokenSerializer = new AccessJwsTokenSerializer(
                new MACSigner(OctetSequenceKey.parse(accessKey)),
                JWSAlgorithm.HS256
        );

        return new JwtService(refreshTokenFactory,
                accessTokenFactory,
                refreshTokenSerializer,
                accessTokenSerializer,
                refreshTokenDeserializer,
                jdbcTemplate
        );
    }


}
