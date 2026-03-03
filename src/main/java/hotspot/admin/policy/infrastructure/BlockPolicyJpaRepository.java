package hotspot.admin.policy.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {

    Page<BlockPolicyEntity> findAllByFamilyIdIsNull(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE block_policy
                    SET is_active = :isActive,
                        modified_time = now()
                    WHERE block_policy_id = :policyId
                      AND family_id IS NULL
                      AND is_deleted = false
                    """,
            nativeQuery = true
    )
    int updateActiveById(@Param("policyId") Long policyId, @Param("isActive") Boolean isActive);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE block_policy
                    SET is_deleted = true,
                        modified_time = now()
                    WHERE block_policy_id = :policyId
                      AND family_id IS NULL
                      AND is_deleted = false
                    """,
            nativeQuery = true
    )
    int softDeleteById(@Param("policyId") Long policyId);

    boolean existsByPolicyNameAndPolicyTypeAndFamilyIdIsNull(String policyName, PolicyType policyType);
}
