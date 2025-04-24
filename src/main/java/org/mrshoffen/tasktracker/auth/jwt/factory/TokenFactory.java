package org.mrshoffen.tasktracker.auth.jwt.factory;

import org.mrshoffen.tasktracker.auth.jwt.JwtToken;

public interface TokenFactory<P> {
    JwtToken generateToken(P payload);
}
