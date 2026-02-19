package hotspot.admin.subscription.infrastructure.entity;

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

import hotspot.admin.common.BaseEntity;
import hotspot.admin.member.infrastructure.entity.MemberEntity;
import hotspot.admin.plan.infrastructure.entity.PlanEntity;
import hotspot.admin.subscription.domain.Subscription;
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
@Table(name = "subscription")
@SQLDelete(sql = "UPDATE subscription SET is_deleted = true WHERE sub_id = ?")
@Where(clause = "is_deleted = false")
public class SubscriptionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_id")
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanEntity plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "phone_enc", length = 255, nullable = false)
    private String phoneEnc;

    @Column(name = "phone_hash", length = 64, nullable = false)
    private String phoneHash;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static SubscriptionEntity domainToEntity(
            Subscription subscription,
            PlanEntity planEntity,
            MemberEntity memberEntity
    ) {
        return SubscriptionEntity.builder()
                .subId(subscription.getSubId())
                .plan(planEntity)
                .member(memberEntity)
                .phoneEnc(subscription.getPhoneEnc())
                .phoneHash(subscription.getPhoneHash())
                .isLocked(subscription.getIsLocked())
                .isDeleted(subscription.getIsDeleted())
                .build();
    }

    public Subscription entityToDomain() {
        return Subscription.builder()
                .subId(subId)
                .planId(plan.getPlanId())
                .memberId(member.getMemberId())
                .phoneEnc(phoneEnc)
                .phoneHash(phoneHash)
                .isLocked(isLocked)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
