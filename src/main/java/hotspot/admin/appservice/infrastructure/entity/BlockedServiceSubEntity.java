package hotspot.admin.appservice.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.admin.appservice.domain.BlockedServiceSub;
import hotspot.admin.common.BaseEntity;
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
@Table(name = "blocked_service_sub")
@SQLDelete(sql = "UPDATE blocked_service_sub SET is_active = false WHERE blocked_service_sub_id = ?")
@Where(clause = "is_active = true")
public class BlockedServiceSubEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blocked_service_sub_id")
    private Long blockedServiceSubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "sub_id", nullable = false, insertable = false, updatable = false)
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_service_id", nullable = false)
    private AppBlockedServiceEntity appBlockedService;

    @Column(name = "blocked_service_id", nullable = false, insertable = false, updatable = false)
    private Long blockedServiceId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public static BlockedServiceSubEntity domainToEntity(
            BlockedServiceSub blockedServiceSub,
            SubscriptionEntity subscriptionEntity,
            AppBlockedServiceEntity appBlockedServiceEntity
    ) {
        return BlockedServiceSubEntity.builder()
                .blockedServiceSubId(blockedServiceSub.getBlockedServiceSubId())
                .subscription(subscriptionEntity)
                .appBlockedService(appBlockedServiceEntity)
                .isActive(blockedServiceSub.getIsActive())
                .build();
    }

    public BlockedServiceSub entityToDomain() {
        return BlockedServiceSub.builder()
                .blockedServiceSubId(blockedServiceSubId)
                .subId(subId)
                .blockedServiceId(blockedServiceId)
                .isActive(isActive)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
