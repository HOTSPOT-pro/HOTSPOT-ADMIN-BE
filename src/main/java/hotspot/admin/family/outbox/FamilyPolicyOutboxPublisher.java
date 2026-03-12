package hotspot.admin.family.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.family.controller.request.PolicyActiveRequest;
import hotspot.admin.family.outbox.dto.FamilyPolicyAlertEvent;
import hotspot.admin.outbox.notificationOutbox.service.NotificationOutboxEventAppender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyPolicyOutboxPublisher {

    private static final String AGGREGATE_TYPE = "user-alert";
    private static final String EVENT_TYPE_TIME_WINDOW_POLICY = "TIME_WINDOW_POLICY";
    private static final String EVENT_TYPE_IMMEDIATE_BLOCK = "IMMEDIATE_BLOCK";
    private static final String EVENT_TYPE_SERVICE_ACCESS = "SERVICE_ACCESS";
    private static final String ALERT_TYPE_APPLIED = "APPLIED";
    private static final String ALERT_TYPE_RELEASED = "RELEASED";

    private final NotificationOutboxEventAppender outboxEventAppender;

    public void publishTimeWindowPolicy(Long familyId, Long subId, List<PolicyActiveRequest> policies) {
        publish(resolvePolicyAlertType(policies), EVENT_TYPE_TIME_WINDOW_POLICY, familyId, subId);
    }

    public void publishServiceAccess(Long familyId, Long subId, List<PolicyActiveRequest> policies) {
        publish(resolvePolicyAlertType(policies), EVENT_TYPE_SERVICE_ACCESS, familyId, subId);
    }

    public void publishImmediateBlock(Long familyId, Long subId, boolean isBlocked) {
        publish(isBlocked ? ALERT_TYPE_APPLIED : ALERT_TYPE_RELEASED, EVENT_TYPE_IMMEDIATE_BLOCK, familyId, subId);
    }

    private void publish(String alertType, String eventType, Long familyId, Long subId) {
        try {
            String aggregateId = familyId != null ? String.valueOf(familyId) : String.valueOf(subId);

            FamilyPolicyAlertEvent event = new FamilyPolicyAlertEvent(
                    UUID.randomUUID().toString(),
                    eventType,
                    alertType,
                    subId,
                    familyId,
                    LocalDateTime.now()
            );

            outboxEventAppender.append(
                    AGGREGATE_TYPE,
                    aggregateId,
                    eventType,
                    event
            );
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_EVENT_PUBLISH_FAILED, ex);
        }
    }

    private String resolvePolicyAlertType(List<PolicyActiveRequest> policies) {
        return policies.stream().anyMatch(PolicyActiveRequest::isActive)
                ? ALERT_TYPE_APPLIED
                : ALERT_TYPE_RELEASED;
    }
}
