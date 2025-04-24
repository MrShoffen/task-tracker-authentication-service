package org.mrshoffen.tasktracker.auth.jwt.deserializer;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mrshoffen.tasktracker.auth.exception.InvalidRefreshTokenException;
import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RefreshJweTokenDeserializer implements TokenDeserializer {

    private final JWEDecrypter jweDecrypter;

    @Override
    public JwtToken deserialize(String token) {
        try {
            var encryptedJWT = EncryptedJWT.parse(token);
            encryptedJWT.decrypt(this.jweDecrypter);
            var claimsSet = encryptedJWT.getJWTClaimsSet();

            Map<String, String> payload = claimsSet.getClaims().entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof String)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> (String) entry.getValue()
                    ));

            return new JwtToken(UUID.fromString(claimsSet.getJWTID()), payload,
                    claimsSet.getIssueTime().toInstant(),
                    claimsSet.getExpirationTime().toInstant());
        } catch (Exception exception) {
            throw new InvalidRefreshTokenException("Некорректный refresh токен!", exception);
        }
    }
}
