package hotspot.admin.common.util.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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
