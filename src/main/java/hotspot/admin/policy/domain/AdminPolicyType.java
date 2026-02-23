package hotspot.admin.policy.domain;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;

public enum AdminPolicyType {
    TIME,
    APP;

    public static AdminPolicyType from(String value) {
        if (value == null) {
            throw new ApplicationException(PolicyErrorCode.INVALID_POLICY_TYPE);
        }

        try {
            return AdminPolicyType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(PolicyErrorCode.INVALID_POLICY_TYPE);
        }
    }
}
