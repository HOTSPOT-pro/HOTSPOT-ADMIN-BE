package hotspot.admin.policy.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
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
@Table(name = "block_policy")
@SQLDelete(sql = "UPDATE block_policy SET is_deleted = true WHERE block_policy_id = ?")
@Where(clause = "is_deleted = false")
public class BlockPolicyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_policy_id")
    private Long blockPolicyId;

    @Column(name = "policy_name", length = 20, nullable = false)
    private String policyName;

    @Column(name = "policy_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_snapshot", columnDefinition = "jsonb", nullable = false)
    private PolicySnapshot policySnapshot;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static BlockPolicyEntity domainToEntity(BlockPolicy blockPolicy) {
        return BlockPolicyEntity.builder()
                .blockPolicyId(blockPolicy.getBlockPolicyId())
                .policyName(blockPolicy.getPolicyName())
                .policyType(blockPolicy.getPolicyType())
                .policySnapshot(blockPolicy.getPolicySnapshot())
                .isActive(blockPolicy.getIsActive())
                .isDeleted(blockPolicy.getIsDeleted())
                .build();
    }

    public BlockPolicy entityToDomain() {
        return BlockPolicy.builder()
                .blockPolicyId(blockPolicyId)
                .policyName(policyName)
                .policyType(policyType)
                .policySnapshot(policySnapshot)
                .isActive(isActive)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
