package hotspot.admin.usage.familyUsage.domain;


import hotspot.admin.common.util.redis.RedisUsageCalculator;

public class FamilySubUsage {

    private final double subLimitKb;
    private final double memberFamilyUsedKb;

    public FamilySubUsage(
            double subLimitKb,
            double memberFamilyUsedKb
    ) {
        this.subLimitKb = subLimitKb;
        this.memberFamilyUsedKb = memberFamilyUsedKb;
    }

    public double remainKb() {
        return RedisUsageCalculator.calculateRemain(subLimitKb, memberFamilyUsedKb);
    }

    public int usagePercent() {
        return RedisUsageCalculator.calculatePercent(memberFamilyUsedKb, subLimitKb);
    }

    public double limitGb() {
        return RedisUsageCalculator.kbToGb(subLimitKb);
    }

    public double familyUsedGb() {
        return RedisUsageCalculator.kbToGb(memberFamilyUsedKb);
    }

    public double remainGb() {
        return RedisUsageCalculator.kbToGb(remainKb());
    }

    public static FamilySubUsage zero() {
        return new FamilySubUsage(0, 0);
    }
}
