package hotspot.admin.family.domain;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;

public enum ApplyType {
    ADD,
    REMOVE;

    public static ApplyType from(String value) {
        if (value == null) {
            throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_TYPE);
        }

        for (ApplyType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_TYPE);
    }
}
