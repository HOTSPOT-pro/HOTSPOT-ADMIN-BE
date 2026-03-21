package hotspot.admin.policy.domain;

public record PolicyPayload(
        Long policyId,
        PolicyType policyType,
        String encoded,
        Long expireEpoch
) {

    public static PolicyPayload scheduled(Long policyId, String encoded) {
        return new PolicyPayload(
                policyId,
                PolicyType.SCHEDULED,
                encoded,
                null
        );
    }

    public static PolicyPayload once(Long policyId, Long expireEpoch) {
        return new PolicyPayload(
                policyId,
                PolicyType.ONCE,
                null,
                expireEpoch
        );
    }
}
