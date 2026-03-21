package hotspot.admin.family.infrastructure.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyApplyTarget;
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
    @JoinColumn(name = "family_id")
    private FamilyEntity family;

    @Column(name = "apply_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyType applyType;

    @Column(name = "doc_url", length = 255)
    private String docUrl;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FamilyApplyStatus status = FamilyApplyStatus.PENDING;

    @OneToMany(mappedBy = "familyApply", fetch = FetchType.LAZY)
    @Builder.Default
    private List<FamilyApplyTargetEntity> targets = new ArrayList<>();

    public static FamilyApplyEntity domainToEntity(
            FamilyApply familyApply,
            SubscriptionEntity requesterSubscriptionEntity,
            FamilyEntity familyEntity
    ) {
        return FamilyApplyEntity.builder()
                .familyApplyId(familyApply.getFamilyApplyId())
                .requesterSubscription(requesterSubscriptionEntity)
                .family(familyEntity)
                .applyType(familyApply.getApplyType())
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus())
                .targets(Collections.emptyList())
                .build();
    }

    public FamilyApply entityToDomain() {
        List<FamilyApplyTarget> targetDomains = targets == null
                ? Collections.emptyList()
                : targets.stream().map(FamilyApplyTargetEntity::toDomain).toList();

        return FamilyApply.builder()
                .familyApplyId(familyApplyId)
                .requesterSubId(requesterSubscription.getSubId())
                .familyId(family == null ? null : family.getFamilyId())
                .applyType(applyType)
                .docUrl(docUrl)
                .status(status)
                .targets(targetDomains)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
