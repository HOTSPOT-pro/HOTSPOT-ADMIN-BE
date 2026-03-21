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

import hotspot.admin.family.domain.FamilyApplyTarget;
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
@Table(name = "family_apply_target")
public class FamilyApplyTargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_apply_target_id")
    private Long familyApplyTargetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_apply_id", nullable = false)
    private FamilyApplyEntity familyApply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sub_id", nullable = false)
    private SubscriptionEntity targetSubscription;

    @Column(name = "target_family_role", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private FamilyRole targetFamilyRole;

    public FamilyApplyTarget toDomain() {
        return FamilyApplyTarget.builder()
                .familyApplyTargetId(familyApplyTargetId)
                .familyApplyId(familyApply == null ? null : familyApply.getFamilyApplyId())
                .targetSubId(targetSubscription == null ? null : targetSubscription.getSubId())
                .targetFamilyRole(targetFamilyRole)
                .build();
    }
}
