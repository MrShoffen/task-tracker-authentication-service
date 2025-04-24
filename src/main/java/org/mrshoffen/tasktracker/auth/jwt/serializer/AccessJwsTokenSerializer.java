package org.mrshoffen.tasktracker.auth.jwt.serializer;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class AccessJwsTokenSerializer implements TokenSerializer {

    private final JWSSigner jwsSigner;

    private final JWSAlgorithm jwsAlgorithm;// = JWSAlgorithm.HS256;

    @Override
    public String serialize(JwtToken token) {
        var jwsHeader = new JWSHeader.Builder(this.jwsAlgorithm)
                .keyID(token.id().toString())
                .build();
        var preBuildClaims = new JWTClaimsSet.Builder()
                .jwtID(token.id().toString())
                .subject(token.id().toString())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()));

        token.payload().forEach(preBuildClaims::claim);

        var signedJWT = new SignedJWT(jwsHeader, preBuildClaims.build());
        try {
            signedJWT.sign(this.jwsSigner);

            return signedJWT.serialize();
        } catch (JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }

        return null;
    }
}
