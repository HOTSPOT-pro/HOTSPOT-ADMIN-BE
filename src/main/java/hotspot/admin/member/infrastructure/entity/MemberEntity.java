package hotspot.admin.member.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.member.domain.Member;
import hotspot.admin.member.domain.SocialAccount;
import hotspot.admin.member.domain.Status;
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
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE member_id = ?")
@Where(clause = "is_deleted = false")
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "birth", length = 6, nullable = false)
    private String birth;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static MemberEntity domainToEntity(Member member) {
        return MemberEntity.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .birth(member.getBirth())
                .status(member.getStatus())
                .isDeleted(member.getIsDeleted())
                .build();
    }

    public Member entityToDomain() {
        return Member.builder()
                .memberId(memberId)
                .name(name)
                .birth(birth)
                .status(status)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }

    public Member entityToDomain(SocialAccount socialAccount) {
        return Member.builder()
                .memberId(memberId)
                .name(name)
                .birth(birth)
                .status(status)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .socialAccount(socialAccount)
                .build();
    }
}
