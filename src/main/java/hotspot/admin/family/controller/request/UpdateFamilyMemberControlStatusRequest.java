package hotspot.admin.family.controller.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public record UpdateFamilyMemberControlStatusRequest(
        @Min(value = 0, message = "dataLimitGb는 0 이상이어야 합니다.")
        Long dataLimitGb,
        Boolean isBlocked,
        Boolean isParent
) {

    @AssertTrue(message = "dataLimitGb, isBlocked, isParent 중 하나 이상은 입력해야 합니다.")
    public boolean hasAnyField() {
        return dataLimitGb != null || isBlocked != null || isParent != null;
    }
}
