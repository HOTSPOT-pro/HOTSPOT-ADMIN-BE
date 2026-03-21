package hotspot.admin.family.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.outbox.dto.FamilyRequestAlertEvent;
import hotspot.admin.outbox.notificationOutbox.service.NotificationOutboxEventAppender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyRequestOutboxPublisher {

    private static final String AGGREGATE_TYPE = "user-alert";
    private static final String EVENT_TYPE_FAMILY_CREATE = "FAMILY_CREATE";
    private static final String EVENT_TYPE_FAMILY_MEMBER_ADD = "FAMILY_MEMBER_ADD";
    private static final String EVENT_TYPE_FAMILY_MEMBER_REMOVE = "FAMILY_MEMBER_REMOVE";
    private static final String TYPE_APPROVED = "APPROVED";
    private static final String TYPE_REJECTED = "REJECTED";

    private final NotificationOutboxEventAppender outboxEventAppender;

    // 가족 요청이 승인되었음을 알리는 Outbox 이벤트 발행을 수행한다.
    public void publishApproved(FamilyApply familyApply, List<String> targetNames, Long familyId) {
        publish(TYPE_APPROVED, familyApply, targetNames, familyId);
    }

    // 가족 요청이 반려되었음을 알리는 Outbox 이벤트 발행을 수행한다.
    public void publishRejected(FamilyApply familyApply, List<String> targetNames, Long familyId) {
        publish(TYPE_REJECTED, familyApply, targetNames, familyId);
    }

    // 승인/반려 타입에 맞는 알림 이벤트를 생성해 Outbox에 적재하고, 실패 시 예외를 공통 처리한다.
    private void publish(String type, FamilyApply familyApply, List<String> targetNames, Long familyId) {
        try {
            Long resolvedFamilyId = familyId != null ? familyId : familyApply.getFamilyId();
            String aggregateId = resolvedFamilyId != null
                    ? String.valueOf(resolvedFamilyId)
                    : String.valueOf(familyApply.getFamilyApplyId());
            String eventType = resolveEventType(familyApply.getApplyType());

            FamilyRequestAlertEvent event = new FamilyRequestAlertEvent(
                    UUID.randomUUID().toString(),
                    eventType,
                    type,
                    targetNames == null ? List.of() : List.copyOf(targetNames),
                    resolvedFamilyId,
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

    private String resolveEventType(ApplyType applyType) {
        if (applyType == null) {
            throw new ApplicationException(OutboxErrorCode.FAMILY_REQUEST_EVENT_BUILD_FAILED);
        }

        switch (applyType) {
            case CREATE:
                return EVENT_TYPE_FAMILY_CREATE;
            case ADD:
                return EVENT_TYPE_FAMILY_MEMBER_ADD;
            case REMOVE:
                return EVENT_TYPE_FAMILY_MEMBER_REMOVE;
            default:
                throw new ApplicationException(OutboxErrorCode.FAMILY_REQUEST_EVENT_BUILD_FAILED);
        }
    }
}
