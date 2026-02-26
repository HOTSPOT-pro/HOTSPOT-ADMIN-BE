package hotspot.admin.family.outbox.dto;

import java.time.LocalDateTime;

public record FamilyRequestAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        String targetName,
        Long familyId,
        LocalDateTime createdTime
) {
}
