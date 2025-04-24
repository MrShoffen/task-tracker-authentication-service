package org.mrshoffen.tasktracker.auth.jwt.serializer;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

import java.util.Date;

import static org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties.UiService.LOGGER;

@RequiredArgsConstructor
@Slf4j
public class RefreshJweTokenSerializer implements TokenSerializer {

    private final JWEEncrypter jweEncrypter;

    private final  JWEAlgorithm jweAlgorithm;// = JWEAlgorithm.DIR;

    private final EncryptionMethod encryptionMethod; // = EncryptionMethod.A128GCM;

    @Override
    public String serialize(JwtToken token) {
        var jwsHeader = new JWEHeader.Builder(this.jweAlgorithm, this.encryptionMethod)
                .keyID(token.id().toString())
                .build();

        var preBuildClaims = new JWTClaimsSet.Builder()
                .jwtID(token.id().toString())
                .subject(token.id().toString())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()));

        token.payload().forEach(preBuildClaims::claim);


        var encryptedJWT = new EncryptedJWT(jwsHeader, preBuildClaims.build());
        try {
            encryptedJWT.encrypt(this.jweEncrypter);

            return encryptedJWT.serialize();
        } catch (JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }

        return null;
    }
}
