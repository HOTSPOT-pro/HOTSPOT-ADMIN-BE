package hotspot.admin.family.domain;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;

public enum FamilyApplyStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED;

    public static FamilyApplyStatus from(String value) {
        if (value == null) {
            throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_STATUS);
        }

        for (FamilyApplyStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_STATUS);
    }
}
