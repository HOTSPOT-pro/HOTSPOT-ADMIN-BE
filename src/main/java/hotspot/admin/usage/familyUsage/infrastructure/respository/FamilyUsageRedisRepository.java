package hotspot.admin.usage.familyUsage.infrastructure.respository;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyUsageErrorCode;
import hotspot.admin.common.util.redis.PipelineResultMapper;
import hotspot.admin.common.util.redis.RedisPipelineExecutor;
import hotspot.admin.common.util.redis.RedisValueParser;
import hotspot.admin.usage.familyUsage.domain.FamilySubUsage;
import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.infrastructure.keybuilder.FamilyUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FamilyUsageRedisRepository {

    private static final String K_FAMILY_LIMIT = "family_limit";
    private static final String K_FAMILY_USED  = "family_used";
    private static final String K_SUB_LIMIT_PREFIX = "sub_limit:";
    private static final String K_SUB_USAGE_PREFIX = "sub_usage:";

    private final RedisPipelineExecutor pipelineExecutor;
    private final Clock clock;

    public FamilyUsage findFamilyAndSubData(Long familyId, List<Long> subIds) {

        PipelineResult pipeline = executePipeline(familyId, subIds);
        Map<String, Object> resultMap = PipelineResultMapper.toMap(pipeline.requestKeys(), pipeline.rawResults());

        Object familyLimitValue = resultMap.get(K_FAMILY_LIMIT);

        if (familyLimitValue == null) {
            throw new ApplicationException(FamilyUsageErrorCode.FAMILY_LIMIT_NOT_FOUND);
        }


        double familyLimitKb = RedisValueParser.toDouble(familyLimitValue);
        double familyUsedKb  = RedisValueParser.toDouble(resultMap.get(K_FAMILY_USED));

        Map<Long, FamilySubUsage> subMap = buildSubUsageMap(subIds, resultMap);

        return new FamilyUsage(familyLimitKb, familyUsedKb, subMap);
    }

    private PipelineResult executePipeline(Long familyId, List<Long> subIds) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults = pipelineExecutor.execute((RedisCallback<Object>) connection -> {

            addFamilyRequests(connection, familyId, requestKeys);
            addSubRequests(connection, familyId, subIds, requestKeys);

            return null;
        });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addFamilyRequests(
            RedisConnection connection,
            Long familyId,
            List<String> requestKeys
    ) {
        requestKeys.add(K_FAMILY_LIMIT);
        connection.hGet(
                pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.familyLimit(familyId)),
                pipelineExecutor.serialize("family_limit")
        );

        requestKeys.add(K_FAMILY_USED);
        connection.hGet(
                pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.familyUsage(familyId, LocalDate.now(clock))),
                pipelineExecutor.serialize("family_used")
        );
    }

    private void addSubRequests(
            RedisConnection connection,
            Long familyId,
            List<Long> subIds,
            List<String> requestKeys
    ) {
        for (Long subId : subIds) {

            requestKeys.add(K_SUB_LIMIT_PREFIX + subId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            FamilyUsageRedisKeyBuilder.familySubLimit(familyId, subId)
                    ),
                    pipelineExecutor.serialize("family_limit")
            );

            requestKeys.add(K_SUB_USAGE_PREFIX + subId);
            connection.hGet(
                    pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.subUsage(subId, LocalDate.now(clock))),
                    pipelineExecutor.serialize("member_family_used")
            );
        }
    }


    private Map<Long, FamilySubUsage> buildSubUsageMap(
            List<Long> subIds,
            Map<String, Object> resultMap
    ) {

        Map<Long, FamilySubUsage> subMap = new HashMap<>(subIds.size());

        for (Long subId : subIds) {
            Object subLimitValue =
                    resultMap.get(K_SUB_LIMIT_PREFIX + subId);

            if (subLimitValue == null) {
                throw new ApplicationException(FamilyUsageErrorCode.FAMILY_SUB_LIMIT_NOT_FOUND);
            }

            double subLimitKb =
                    RedisValueParser.toDouble(subLimitValue);

            double memberFamilyUsedKb =
                    RedisValueParser.toDouble(
                            resultMap.get(
                                    K_SUB_USAGE_PREFIX + subId
                            )
                    );

            subMap.put(subId, new FamilySubUsage(subLimitKb, memberFamilyUsedKb));
        }

        return subMap;
    }

    private record PipelineResult(
            List<String> requestKeys,
            List<Object> rawResults
    ) {}
}
