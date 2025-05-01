package org.mrshoffen.tasktracker.auth.util.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BlacklistTokenHolder {
    private final StringRedisTemplate redisTemplate;


    public void addTokenToBlackList(String token, Duration ttl) {
        redisTemplate.opsForValue()
                .set("blacklist:" + token,
                        "logged_out",
                        ttl);
    }

    public boolean tokenInBlackList(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}
