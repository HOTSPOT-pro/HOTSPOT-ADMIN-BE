package hotspot.admin.subscription.service.port;

public interface SubscriptionRepository {

     boolean findIsLockedBySubId(Long subId);
}
