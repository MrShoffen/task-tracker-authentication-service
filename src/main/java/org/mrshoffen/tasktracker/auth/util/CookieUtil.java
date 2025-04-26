package org.mrshoffen.tasktracker.auth.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@UtilityClass
public class CookieUtil {

    public static ResponseCookie clearCookie(String cookieName) {
        return ResponseCookie
                .from(cookieName, "")
                .path("/")
                .maxAge(0)
                .build();
    }

    public static ResponseCookie buildCookie(String cookieName, String cookieValue, Duration maxAge) {
        return ResponseCookie
                .from(cookieName, cookieValue)
                .maxAge(maxAge)
                .path("/")
                .httpOnly(true)
                .build();
    }
}
