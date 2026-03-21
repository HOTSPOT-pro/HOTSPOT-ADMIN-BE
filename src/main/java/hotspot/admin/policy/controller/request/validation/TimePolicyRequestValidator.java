package hotspot.admin.policy.controller.request.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

public class TimePolicyRequestValidator
        implements ConstraintValidator<ValidTimePolicyRequest, CreateTimePolicyRequest> {

    @Override
    public boolean isValid(CreateTimePolicyRequest request, ConstraintValidatorContext context) {
        if (request == null || request.policyType() == null || request.policySnapshot() == null) {
            return false;
        }

        PolicyType policyType = request.policyType();
        PolicySnapshot snapshot = request.policySnapshot();

        return switch (policyType) {
            case SCHEDULED -> snapshot.isScheduledPolicy() && snapshot.getDurationMinutes() == null;
            case ONCE -> snapshot.isOncePolicy() && (snapshot.getDays() == null || snapshot.getDays().isEmpty());
        };
    }
}
