package hotspot.admin.family.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
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
@Table(name = "family_apply")
public class FamilyApplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_apply_id")
    private Long familyApplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_sub_id", nullable = false)
    private SubscriptionEntity requesterSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sub_id", nullable = false)
    private SubscriptionEntity targetSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @Column(name = "apply_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyType applyType;

    @Column(name = "target_family_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private FamilyRole targetFamilyRole;

    @Column(name = "doc_url", length = 100)
    private String docUrl;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FamilyApplyStatus status = FamilyApplyStatus.PENDING;

    public static FamilyApplyEntity domainToEntity(
            FamilyApply familyApply,
            SubscriptionEntity requesterSubscriptionEntity,
            SubscriptionEntity targetSubscriptionEntity,
            FamilyEntity familyEntity
    ) {
        return FamilyApplyEntity.builder()
                .familyApplyId(familyApply.getFamilyApplyId())
                .requesterSubscription(requesterSubscriptionEntity)
                .targetSubscription(targetSubscriptionEntity)
                .family(familyEntity)
                .applyType(familyApply.getApplyType())
                .targetFamilyRole(familyApply.getTargetFamilyRole())
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus())
                .build();
    }

    public FamilyApply entityToDomain() {
        return FamilyApply.builder()
                .familyApplyId(familyApplyId)
                .requesterSubId(requesterSubscription.getSubId())
                .targetSubId(targetSubscription.getSubId())
                .familyId(family.getFamilyId())
                .applyType(applyType)
                .targetFamilyRole(targetFamilyRole)
                .docUrl(docUrl)
                .status(status)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
