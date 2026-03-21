package hotspot.admin.family.outbox.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FamilyRequestAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        List<String> targetNames,
        Long familyId,
        LocalDateTime createdTime
) {
}
