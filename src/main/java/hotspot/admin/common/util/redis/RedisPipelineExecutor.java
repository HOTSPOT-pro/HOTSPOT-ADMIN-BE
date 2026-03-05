package hotspot.admin.common.util.redis;

import java.util.List;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisPipelineExecutor {

    private final StringRedisTemplate redisTemplate;

    public List<Object> execute(RedisCallback<Object> callback) {
        return redisTemplate.executePipelined(callback);
    }

    public byte[] serialize(String value) {
        return redisTemplate.getStringSerializer().serialize(value);
    }
}
