package org.mrshoffen.tasktracker.auth.util.jwt.deserializer;

import org.mrshoffen.tasktracker.auth.util.jwt.JwtToken;

public interface TokenDeserializer {

    JwtToken deserialize(String token);
}
