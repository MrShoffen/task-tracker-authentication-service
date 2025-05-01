package org.mrshoffen.tasktracker.auth.util.jwt.serializer;

import org.mrshoffen.tasktracker.auth.util.jwt.JwtToken;

public interface TokenSerializer {

    String serialize(JwtToken token);
}
