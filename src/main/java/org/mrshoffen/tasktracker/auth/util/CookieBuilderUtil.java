package org.mrshoffen.tasktracker.auth.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@UtilityClass
public class CookieBuilderUtil {

    public static ResponseCookie clear(String cookieName) {
        return ResponseCookie
                .from(cookieName, "")
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
    }

    public static ResponseCookie withNameAndValue(String cookieName, String cookieValue, Duration maxAge) {
        return ResponseCookie
                .from(cookieName, cookieValue)
                .maxAge(maxAge)
                .secure(false)
                .path("/")
                .httpOnly(true)
                .build();
    }
}
