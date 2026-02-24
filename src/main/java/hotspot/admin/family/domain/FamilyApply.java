package hotspot.admin.family.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilyApply {
    private final Long familyApplyId;
    private final Long requesterSubId;
    private final Long targetSubId;
    private final Long familyId;
    private final ApplyType applyType;
    private final FamilyRole tagetFamilyRole;
    private final String docUrl;
    private final FamilyApplyStatus status;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
