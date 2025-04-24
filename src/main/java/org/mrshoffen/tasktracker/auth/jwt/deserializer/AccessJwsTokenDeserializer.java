package org.mrshoffen.tasktracker.auth.jwt.deserializer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class AccessJwsTokenDeserializer implements TokenDeserializer {

    private final JWSVerifier jwsVerifier;

    @Override
    public JwtToken deserialize(String token) {
        try {
            var signedJWT = SignedJWT.parse(token);

            if (signedJWT.verify(this.jwsVerifier)) {
                var claimsSet = signedJWT.getJWTClaimsSet();

                Map<String, String> payload = claimsSet.getClaims().entrySet().stream()
                        .filter(entry -> entry.getValue() instanceof String)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (String) entry.getValue()
                        ));


                return new JwtToken(UUID.fromString(claimsSet.getJWTID()), payload,
                        claimsSet.getIssueTime().toInstant(),
                        claimsSet.getExpirationTime().toInstant());
            }
        } catch (ParseException | JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }
        return null;
    }
}
