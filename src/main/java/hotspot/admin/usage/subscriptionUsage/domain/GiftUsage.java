package hotspot.admin.usage.subscriptionUsage.domain;

import hotspot.admin.common.util.redis.RedisUsageCalculator;

public class GiftUsage {

    private final Long giftId;
    private final double limitKb;
    private final double usedKb;

    public GiftUsage(Long giftId, double limitKb, double usedKb) {
        this.giftId = giftId;
        this.limitKb = limitKb;
        this.usedKb = usedKb;
    }

    public Long giftId() {
        return giftId;
    }

    public double limitKb() { return limitKb; }

    public double usedKb() { return usedKb; }

    public double remainKb() {
        return RedisUsageCalculator.calculateRemain(limitKb, usedKb);
    }

    public int usagePercent() {
        return RedisUsageCalculator.calculatePercent(usedKb, limitKb);
    }

    public double limitGb() {
        return RedisUsageCalculator.kbToGb(limitKb);
    }

    public double usedGb() {
        return RedisUsageCalculator.kbToGb(usedKb);
    }

    public double remainGb() {
        return RedisUsageCalculator.kbToGb(remainKb());
    }
}
