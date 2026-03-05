package hotspot.admin.usage.subscriptionUsage.infrastructure.repository;

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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRedisRepository {

    private static final String K_PLAN_LIMIT = "plan_limit";
    private static final String K_PLAN_USED  = "plan_used";
    private static final String K_GIFT_LIMIT_PREFIX = "gift_limit:";
    private static final String K_GIFT_USED_PREFIX  = "gift_used:";

    private final RedisPipelineExecutor pipelineExecutor;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public Map<Long, SubscriptionUsage> findSubscriptionUsages(
            Map<Long, DataPeriod> subPeriodMap
    ) {

        LocalDate now = LocalDate.now(clock);

        List<String> requestKeys = new ArrayList<>();
        List<Long> subIds = new ArrayList<>(subPeriodMap.keySet());

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    for (Long subId : subIds) {

                        DataPeriod period = subPeriodMap.get(subId);

                        addPlanRequests(connection, subId, period, requestKeys, now);

                    }

                    return null;
                });

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(requestKeys, rawResults);

        Map<Long, SubscriptionUsage> result = new HashMap<>();

        for (Long subId : subIds) {

            double limit =
                    RedisValueParser.toDouble(
                            resultMap.get("plan_limit:" + subId)
                    );

            double used =
                    resultMap.get("plan_used:" + subId) == null
                            ? 0
                            : RedisValueParser.toDouble(
                            resultMap.get("plan_used:" + subId)
                    );

            result.put(
                    subId,
                    new SubscriptionUsage(
                            subId,
                            limit,
                            used,
                            List.of()
                    )
            );
        }

        return result;
    }



    private PipelineResult executePipeline(
            Long subId,
            List<String> giftIds,
            LocalDate now,
            DataPeriod dataPeriod
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    addPlanRequests(connection, subId, dataPeriod, requestKeys, now);
                    addGiftRequests(connection, subId, giftIds, requestKeys, now);

                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addPlanRequests(
            RedisConnection connection,
            Long subId,
            DataPeriod dataPeriod,
            List<String> requestKeys,
            LocalDate now
    ) {

        requestKeys.add(K_PLAN_LIMIT + ":" + subId);

        connection.hGet(
                pipelineExecutor.serialize(
                        SubscriptionUsageRedisKeyBuilder.planLimit(subId)
                ),
                pipelineExecutor.serialize(K_PLAN_LIMIT)
        );

        requestKeys.add(K_PLAN_USED + ":" + subId);

        String usageKey =
                (dataPeriod == DataPeriod.MONTH)
                        ? SubscriptionUsageRedisKeyBuilder.planUsageMonth(subId, now)
                        : SubscriptionUsageRedisKeyBuilder.planUsageDay(subId, now);

        connection.hGet(
                pipelineExecutor.serialize(usageKey),
                pipelineExecutor.serialize(K_PLAN_USED)
        );
    }

    private void addGiftRequests(
            RedisConnection connection,
            Long subId,
            List<String> giftIds,
            List<String> requestKeys,
            LocalDate now
    ) {

        if (giftIds == null || giftIds.isEmpty()) {
            return;
        }

        for (String giftIdStr : giftIds) {

            Long giftId = Long.parseLong(giftIdStr);

            requestKeys.add(K_GIFT_LIMIT_PREFIX + giftId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftLimit(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_limit")
            );

            requestKeys.add(K_GIFT_USED_PREFIX + giftId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftUsage(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_used")
            );
        }
    }

    private SubscriptionUsage buildDomain(
            Long subId,
            List<String> giftIds,
            Map<String, Object> resultMap
    ) {

        Object planLimitValue = resultMap.get(K_PLAN_LIMIT);

        if (planLimitValue == null) {
            throw new ApplicationException(
                    SubscriptionUsageErrorCode.SUBSCRIPTION_LIMIT_NOT_FOUND
            );
        }

        double planLimitKb =
                RedisValueParser.toDouble(planLimitValue);

        Object planUsedValue = resultMap.get(K_PLAN_USED);

        double planUsedKb = planUsedValue == null
                ? 0D
                : RedisValueParser.toDouble(planUsedValue);

        List<GiftUsage> gifts =
                buildGiftUsageList(giftIds, resultMap);

        return new SubscriptionUsage(
                subId,
                planLimitKb,
                planUsedKb,
                gifts
        );
    }

    private List<GiftUsage> buildGiftUsageList(
            List<String> giftIds,
            Map<String, Object> resultMap
    ) {

        List<GiftUsage> gifts = new ArrayList<>();

        for (String giftIdStr : giftIds) {

            Long giftId = Long.parseLong(giftIdStr);

            double limitKb =
                    resultMap.get(K_GIFT_LIMIT_PREFIX + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(K_GIFT_LIMIT_PREFIX + giftId)
                    );

            double usedKb =
                    resultMap.get(K_GIFT_USED_PREFIX + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(K_GIFT_USED_PREFIX + giftId)
                    );

            gifts.add(new GiftUsage(giftId, limitKb, usedKb));
        }

        return gifts;
    }

    private record PipelineResult(
            List<String> requestKeys,
            List<Object> rawResults
    ) {}

    /**
     * 개인 데이터 한도와 개인 데이터 사용량을 Redis에서 조회
     * 남은 잔여량을 계산 후 반환
     */
    public long findRemainingPlanKb(
            Long subId,
            DataPeriod dataPeriod
    ) {

        LocalDate now = LocalDate.now(clock);

        PipelineResult pipeline =
                executePlanOnlyPipeline(subId, dataPeriod, now);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys(),
                        pipeline.rawResults()
                );

        return buildRemaining(resultMap);
    }

    private PipelineResult executePlanOnlyPipeline(
            Long subId,
            DataPeriod dataPeriod,
            LocalDate now
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    addPlanRequests(connection, subId, dataPeriod, requestKeys, now);
                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private long buildRemaining(Map<String, Object> resultMap) {

        Object planLimitValue = resultMap.get(K_PLAN_LIMIT);

        if (planLimitValue == null) {
            throw new ApplicationException(
                    SubscriptionUsageErrorCode.SUBSCRIPTION_LIMIT_NOT_FOUND
            );
        }

        long planLimitKb = RedisValueParser.toLong(planLimitValue);

        Object planUsedValue = resultMap.get(K_PLAN_USED);

        long planUsedKb = (planUsedValue == null)
                ? 0L
                : RedisValueParser.toLong(planUsedValue);

        return Math.max(planLimitKb - planUsedKb, 0L);
    }
}
