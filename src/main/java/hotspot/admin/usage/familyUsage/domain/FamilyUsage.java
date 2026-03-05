package hotspot.admin.usage.familyUsage.domain;


import java.util.Map;

import hotspot.admin.common.util.redis.RedisUsageCalculator;

public class FamilyUsage {

    private final double familyLimitKb;
    private final double familyUsedKb;
    private final Map<Long, FamilySubUsage> subData;

    public FamilyUsage(
            double familyLimitKb,
            double familyUsedKb,
            Map<Long, FamilySubUsage> subData
    ) {
        this.familyLimitKb = familyLimitKb;
        this.familyUsedKb = familyUsedKb;
        this.subData = subData;
    }

    public double familyRemainKb() {
        return RedisUsageCalculator.calculateRemain(familyLimitKb, familyUsedKb);
    }

    public int familyUsagePercent() {
        return RedisUsageCalculator.calculatePercent(familyUsedKb, familyLimitKb);
    }

    public double familyLimitGb() {
        return RedisUsageCalculator.kbToGb(familyLimitKb);
    }

    public double familyUsedGb() {
        return RedisUsageCalculator.kbToGb(familyUsedKb);
    }

    public double familyRemainGb() {
        return RedisUsageCalculator.kbToGb(familyRemainKb());
    }

    public FamilySubUsage getSubOrZero(Long subId) {
        return subData.getOrDefault(subId, FamilySubUsage.zero());
    }
}
