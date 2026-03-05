package hotspot.admin.usage.subscriptionUsage.infrastructure.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.SubscriptionUsageErrorCode;
import hotspot.admin.common.util.redis.PipelineResultMapper;
import hotspot.admin.common.util.redis.RedisPipelineExecutor;
import hotspot.admin.common.util.redis.RedisValueParser;
import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.admin.usage.subscriptionUsage.infrastructure.keybuilder.SubscriptionUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRedisRepository {

    private static final String PLAN_LIMIT = "plan_limit";
    private static final String PLAN_USED  = "plan_used";
    private static final String GIFT_LIMIT = "gift_limit";
    private static final String GIFT_USED  = "gift_used";

    private final RedisPipelineExecutor pipelineExecutor;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public Map<Long, SubscriptionUsage> findSubscriptionUsages(
            Map<Long, DataPeriod> subPeriodMap
    ) {

        if (subPeriodMap == null || subPeriodMap.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate now = LocalDate.now(clock);

        List<Long> subIds = new ArrayList<>(subPeriodMap.keySet());

        // gift index 조회
        Map<Long, List<Long>> giftIdsMap = fetchGiftIds(subIds, now);

        // pipeline 실행
        PipelineResult pipeline =
                executePipeline(subIds, subPeriodMap, giftIdsMap, now);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys,
                        pipeline.rawResults
                );

        Map<Long, SubscriptionUsage> result = new HashMap<>();

        for (Long subId : subIds) {

            List<Long> giftIds =
                    giftIdsMap.getOrDefault(subId, List.of());

            SubscriptionUsage usage =
                    buildDomain(subId, giftIds, resultMap);

            result.put(subId, usage);
        }

        return result;
    }

    private Map<Long, List<Long>> fetchGiftIds(
            List<Long> subIds,
            LocalDate now
    ) {

        Map<Long, List<Long>> result = new HashMap<>();

        for (Long subId : subIds) {

            Set<String> giftIdStrings =
                    redisTemplate.opsForZSet().range(
                            SubscriptionUsageRedisKeyBuilder.giftIndex(subId, now),
                            0,
                            -1
                    );

            if (giftIdStrings == null || giftIdStrings.isEmpty()) {
                result.put(subId, List.of());
                continue;
            }

            List<Long> giftIds =
                    giftIdStrings.stream()
                            .map(Long::parseLong)
                            .toList();

            result.put(subId, giftIds);
        }

        return result;
    }

    private PipelineResult executePipeline(
            List<Long> subIds,
            Map<Long, DataPeriod> subPeriodMap,
            Map<Long, List<Long>> giftIdsMap,
            LocalDate now
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    for (Long subId : subIds) {

                        DataPeriod period =
                                subPeriodMap.get(subId);

                        addPlanRequests(
                                connection,
                                subId,
                                period,
                                requestKeys,
                                now
                        );

                        List<Long> giftIds =
                                giftIdsMap.getOrDefault(subId, List.of());

                        addGiftRequests(
                                connection,
                                subId,
                                giftIds,
                                requestKeys,
                                now
                        );
                    }

                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addPlanRequests(
            RedisConnection connection,
            Long subId,
            DataPeriod period,
            List<String> requestKeys,
            LocalDate now
    ) {

        requestKeys.add(PLAN_LIMIT + ":" + subId);

        connection.hGet(
                pipelineExecutor.serialize(
                        SubscriptionUsageRedisKeyBuilder.planLimit(subId)
                ),
                pipelineExecutor.serialize(PLAN_LIMIT)
        );

        requestKeys.add(PLAN_USED + ":" + subId);

        String usageKey =
                period == DataPeriod.MONTH
                        ? SubscriptionUsageRedisKeyBuilder.planUsageMonth(subId, now)
                        : SubscriptionUsageRedisKeyBuilder.planUsageDay(subId, now);

        connection.hGet(
                pipelineExecutor.serialize(usageKey),
                pipelineExecutor.serialize(PLAN_USED)
        );
    }

    private void addGiftRequests(
            RedisConnection connection,
            Long subId,
            List<Long> giftIds,
            List<String> requestKeys,
            LocalDate now
    ) {

        if (giftIds == null || giftIds.isEmpty()) {
            return;
        }

        for (Long giftId : giftIds) {

            requestKeys.add(GIFT_LIMIT + ":" + subId + ":" + giftId);

            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftLimit(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize(GIFT_LIMIT)
            );

            requestKeys.add(GIFT_USED + ":" + subId + ":" + giftId);

            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftUsage(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize(GIFT_USED)
            );
        }
    }

    private SubscriptionUsage buildDomain(
            Long subId,
            List<Long> giftIds,
            Map<String, Object> resultMap
    ) {

        Object planLimitValue =
                resultMap.get(PLAN_LIMIT + ":" + subId);

        if (planLimitValue == null) {
            throw new ApplicationException(
                    SubscriptionUsageErrorCode.SUBSCRIPTION_LIMIT_NOT_FOUND
            );
        }

        double planLimitKb =
                RedisValueParser.toDouble(planLimitValue);

        Object planUsedValue =
                resultMap.get(PLAN_USED + ":" + subId);

        double planUsedKb =
                planUsedValue == null
                        ? 0D
                        : RedisValueParser.toDouble(planUsedValue);

        List<GiftUsage> gifts =
                buildGiftUsageList(subId, giftIds, resultMap);

        return new SubscriptionUsage(
                subId,
                planLimitKb,
                planUsedKb,
                gifts
        );
    }

    private List<GiftUsage> buildGiftUsageList(
            Long subId,
            List<Long> giftIds,
            Map<String, Object> resultMap
    ) {

        if (giftIds == null || giftIds.isEmpty()) {
            return List.of();
        }

        List<GiftUsage> gifts = new ArrayList<>();

        for (Long giftId : giftIds) {

            double limitKb =
                    resultMap.get(GIFT_LIMIT + ":" + subId + ":" + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(GIFT_LIMIT + ":" + subId + ":" + giftId)
                    );

            double usedKb =
                    resultMap.get(GIFT_USED + ":" + subId + ":" + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(GIFT_USED + ":" + subId + ":" + giftId)
                    );

            gifts.add(
                    new GiftUsage(
                            giftId,
                            limitKb,
                            usedKb
                    )
            );
        }

        return gifts;
    }

    private static final class PipelineResult {

        private final List<String> requestKeys;
        private final List<Object> rawResults;

        private PipelineResult(
                List<String> requestKeys,
                List<Object> rawResults
        ) {
            this.requestKeys = requestKeys;
            this.rawResults = rawResults;
        }
    }
}
