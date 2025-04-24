package org.mrshoffen.tasktracker.auth.jwt.serializer;

import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

public interface TokenSerializer {

    String serialize(JwtToken token);
}
