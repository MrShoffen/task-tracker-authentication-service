package org.mrshoffen.tasktracker.auth.util.jwt.factory;

import org.mrshoffen.tasktracker.auth.util.jwt.JwtToken;

public interface TokenFactory<P> {
    JwtToken generateToken(P payload);
}
