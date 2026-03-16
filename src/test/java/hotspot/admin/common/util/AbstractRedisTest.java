package hotspot.admin.common.util;

import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import hotspot.admin.common.RedisContainerTestConfig;

@DataRedisTest
@ActiveProfiles("test")
@Import(RedisContainerTestConfig.class)
public abstract class AbstractRedisTest {
}
