package hotspot.admin.policy.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.policy.domain.PolicySub;
import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "policy_sub")
public class PolicySubEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_sub_id")
    private Long policySubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "sub_id", nullable = false, insertable = false, updatable = false)
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_policy_id", nullable = false)
    private BlockPolicyEntity blockPolicy;

    @Column(name = "block_policy_id", nullable = false, insertable = false, updatable = false)
    private Long blockPolicyId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public static PolicySubEntity domainToEntity(
            PolicySub policySub,
            SubscriptionEntity subscriptionEntity,
            BlockPolicyEntity blockPolicyEntity
    ) {
        return PolicySubEntity.builder()
                .policySubId(policySub.getPolicySubId())
                .subscription(subscriptionEntity)
                .blockPolicy(blockPolicyEntity)
                .isActive(policySub.getIsActive())
                .build();
    }

    public PolicySub entityToDomain() {
        return PolicySub.builder()
                .policySubId(policySubId)
                .subId(subId)
                .blockPolicyId(blockPolicyId)
                .isActive(isActive)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
