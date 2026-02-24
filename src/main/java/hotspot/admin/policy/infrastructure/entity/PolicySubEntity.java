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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.policy.domain.DateSnapshot;
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
@SQLDelete(sql = "UPDATE policy_sub SET is_deleted = true WHERE policy_sub_id = ?")
@Where(clause = "is_deleted = false")
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

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "date_snapshot", columnDefinition = "jsonb", nullable = false)
    private DateSnapshot dateSnapshot;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static PolicySubEntity domainToEntity(
            PolicySub policySub,
            SubscriptionEntity subscriptionEntity
    ) {
        return PolicySubEntity.builder()
                .policySubId(policySub.getPolicySubId())
                .subscription(subscriptionEntity)
                .policyId(policySub.getPolicyId())
                .dateSnapshot(policySub.getDateSnapshot())
                .isDeleted(policySub.getIsDeleted())
                .build();
    }

    public PolicySub entityToDomain() {
        return PolicySub.builder()
                .policySubId(policySubId)
                .subId(subId)
                .policyId(policyId)
                .dateSnapshot(dateSnapshot)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
