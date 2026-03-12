package hotspot.admin.family.outbox.dto;

import java.time.LocalDateTime;

public record FamilyPolicyAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        Long subId,
        Long familyId,
        LocalDateTime createdTime
) {
}
