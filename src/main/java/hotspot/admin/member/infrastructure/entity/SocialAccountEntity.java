package hotspot.admin.member.infrastructure.entity;

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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.member.domain.Provider;
import hotspot.admin.member.domain.SocialAccount;
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
@Table(name = "social_account")
@SQLDelete(sql = "UPDATE social_account SET is_deleted = true WHERE social_account_id = ?")
@Where(clause = "is_deleted = false")
public class SocialAccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long socialAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "email", length = 20, nullable = false)
    private String email;

    @Column(name = "social_id", length = 20, nullable = false)
    private String socialId;

    @Column(name = "provider", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static SocialAccountEntity domainToEntity(SocialAccount socialAccount, MemberEntity memberEntity) {
        return SocialAccountEntity.builder()
                .socialAccountId(socialAccount.getSocialAccountId())
                .member(memberEntity)
                .email(socialAccount.getEmail())
                .socialId(socialAccount.getSocialId())
                .provider(socialAccount.getProvider())
                .isDeleted(socialAccount.getIsDeleted())
                .build();
    }

    public SocialAccount entityToDomain() {
        return SocialAccount.builder()
                .socialAccountId(socialAccountId)
                .memberId(member.getMemberId())
                .email(email)
                .socialId(socialId)
                .provider(provider)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
