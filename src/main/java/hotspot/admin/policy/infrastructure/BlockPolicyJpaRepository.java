package hotspot.admin.policy.infrastructure;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE block_policy
                    SET is_active = :isActive,
                        modified_time = now()
                    WHERE block_policy_id = :policyId
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
                      AND is_deleted = false
                    """,
            nativeQuery = true
    )
    int softDeleteById(@Param("policyId") Long policyId);

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM block_policy
                        WHERE policy_name = :policyName
                          AND policy_type = :policyType
                          AND is_deleted = false
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByPolicyNameAndPolicyType(@Param("policyName") String policyName,
                                            @Param("policyType") String policyType);
}
