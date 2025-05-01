package org.mrshoffen.tasktracker.auth.authentication.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BlacklistTokenRepository {

    private static final String BLACKLIST_TOKEN_PREFIX_KEY = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void save(String token, Duration ttl) {
        redisTemplate.opsForValue()
                .set(BLACKLIST_TOKEN_PREFIX_KEY + token,
                        "logged_out",
                        ttl);
    }

    public boolean tokenInBlackList(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_TOKEN_PREFIX_KEY + token));
    }
}
