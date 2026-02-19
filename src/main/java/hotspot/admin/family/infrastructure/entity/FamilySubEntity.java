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

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.FamilySub;
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
@Table(name = "family_sub")
public class FamilySubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_sub_id")
    private Long familySubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @Column(name = "family_role", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private FamilyRole familyRole;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "data_limit")
    private Long dataLimit;

    public static FamilySubEntity domainToEntity(
            FamilySub familySub,
            SubscriptionEntity subscriptionEntity,
            FamilyEntity familyEntity
    ) {
        return FamilySubEntity.builder()
                .familySubId(familySub.getFamilySubId())
                .subscription(subscriptionEntity)
                .family(familyEntity)
                .familyRole(familySub.getFamilyRole())
                .priority(familySub.getPriority())
                .dataLimit(familySub.getDataLimit())
                .build();
    }

    public FamilySub entityToDomain() {
        return FamilySub.builder()
                .familySubId(familySubId)
                .subId(subscription.getSubId())
                .familyId(family.getFamilyId())
                .familyRole(familyRole)
                .priority(priority)
                .dataLimit(dataLimit)
                .build();
    }
}
