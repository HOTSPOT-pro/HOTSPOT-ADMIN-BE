package hotspot.admin.family.infrastructure.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.service.port.FamilyPolicyAssignmentRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyPolicyAssignmentRepositoryImpl implements FamilyPolicyAssignmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Set<Long> findExistingTimePolicyIds(Long familyId, Set<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return Set.of();
        }

        String sql = """
                SELECT bp.block_policy_id
                FROM block_policy bp
                WHERE bp.family_id = :familyId
                  AND bp.is_deleted = false
                  AND bp.block_policy_id IN (:policyIds)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("policyIds", policyIds);

        List<Long> ids = jdbcTemplate.queryForList(sql, params, Long.class);
        return new HashSet<>(ids);
    }

    @Override
    public Set<Long> findExistingAppPolicyIds(Set<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return Set.of();
        }

        String sql = """
                SELECT abs.app_blocked_service_id
                FROM app_blocked_service abs
                WHERE abs.is_deleted = false
                  AND abs.app_blocked_service_id IN (:policyIds)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("policyIds", policyIds);

        List<Long> ids = jdbcTemplate.queryForList(sql, params, Long.class);
        return new HashSet<>(ids);
    }

    @Override
    public int updateMemberTimePolicyActive(Long subId, Long policyId, boolean isActive) {
        String sql = """
                UPDATE policy_sub
                SET is_active = :isActive,
                    modified_time = now()
                WHERE sub_id = :subId
                  AND block_policy_id = :policyId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public void insertMemberTimePolicy(Long subId, Long policyId, boolean isActive) {
        String sql = """
                INSERT INTO policy_sub (sub_id, block_policy_id, is_active, created_time, modified_time)
                VALUES (:subId, :policyId, :isActive, now(), now())
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public int updateMemberAppPolicyActive(Long subId, Long policyId, boolean isActive) {
        String sql = """
                UPDATE blocked_service_sub
                SET is_active = :isActive,
                    modified_time = now()
                WHERE sub_id = :subId
                  AND blocked_service_id = :policyId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public void insertMemberAppPolicy(Long subId, Long policyId) {
        String sql = """
                INSERT INTO blocked_service_sub (sub_id, blocked_service_id, is_active, created_time, modified_time)
                VALUES (:subId, :policyId, true, now(), now())
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId);

        jdbcTemplate.update(sql, params);
    }
}
