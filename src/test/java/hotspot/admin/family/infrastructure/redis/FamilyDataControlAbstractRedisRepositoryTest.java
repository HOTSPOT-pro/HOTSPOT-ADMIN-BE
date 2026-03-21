package hotspot.admin.family.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import hotspot.admin.common.util.AbstractRedisTest;
import hotspot.admin.common.util.redis.RedisPipelineExecutor;
import hotspot.admin.family.infrastructure.schema.FamilyDataControl;

@Import({
        FamilyDataControlRedisRepository.class,
        FamilyDataControlAbstractRedisRepositoryTest.RedisTestConfig.class
})
class FamilyDataControlAbstractRedisRepositoryTest extends AbstractRedisTest {

    @Autowired
    FamilyDataControlRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @TestConfiguration
    static class RedisTestConfig {

        @Bean
        RedisPipelineExecutor redisPipelineExecutor(
                StringRedisTemplate redisTemplate
        ) {
            return new RedisPipelineExecutor(redisTemplate);
        }
    }

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    @DisplayName("가족 데이터 제어 정보 조회 성공")
    void shouldReturnFamilyDataControl() {

        String yyyyMM = "202603";

        // 가족 구성원
        redisTemplate.opsForSet().add(
                "idx:family:subs:1",
                "1",
                "2"
        );

        // 가족 전체 limit 30GB
        redisTemplate.opsForHash().put(
                "limit:family:1",
                "family_limit",
                "31457280"
        );

        // 구성원1 usage 5GB
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "member_family_used",
                "5242880"
        );

        // 구성원2 usage 2GB
        redisTemplate.opsForHash().put(
                "usage:sub:2:" + yyyyMM,
                "member_family_used",
                "2097152"
        );

        FamilyDataControl result =
                repository.findFamilyDataLimit(1L);

        assertEquals(30L, result.familyDataLimit());
        assertEquals(2, result.subFamilies().size());

        FamilyDataControl.SubFamilyDataControl sub1 =
                result.subFamilies().stream()
                        .filter(s -> s.subId().equals(1L))
                        .findFirst()
                        .orElseThrow();

        assertEquals(5L, sub1.familyDataUsage());

        FamilyDataControl.SubFamilyDataControl sub2 =
                result.subFamilies().stream()
                        .filter(s -> s.subId().equals(2L))
                        .findFirst()
                        .orElseThrow();

        assertEquals(2L, sub2.familyDataUsage());
    }
}
