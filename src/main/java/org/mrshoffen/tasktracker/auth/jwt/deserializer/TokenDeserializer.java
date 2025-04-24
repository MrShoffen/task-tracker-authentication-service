package org.mrshoffen.tasktracker.auth.jwt.deserializer;

import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

public interface TokenDeserializer {

    JwtToken deserialize(String token);
}
