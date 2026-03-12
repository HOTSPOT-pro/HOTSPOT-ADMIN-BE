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

    private static final String EXCLUDED_FAMILY_APP_POLICY_CODE = "PRESENT_DATA";

    private static final String SQL_FIND_EXISTING_TIME_POLICY_IDS = """
            SELECT bp.block_policy_id
            FROM block_policy bp
            WHERE bp.family_id = :familyId
              AND bp.is_deleted = false
              AND bp.block_policy_id IN (:policyIds)
            """;

    private static final String SQL_FIND_EXISTING_APP_POLICY_IDS = """
            SELECT abs.app_blocked_service_id
            FROM app_blocked_service abs
            WHERE abs.is_active = true
              AND abs.is_deleted = false
              AND abs.blocked_service_code <> :excludedPolicyCode
              AND abs.app_blocked_service_id IN (:policyIds)
            """;

    private static final String SQL_UPDATE_MEMBER_TIME_POLICY_ACTIVE = """
            UPDATE policy_sub
            SET is_active = :isActive,
                modified_time = now()
            WHERE sub_id = :subId
              AND block_policy_id = :policyId
            """;

    private static final String SQL_INSERT_MEMBER_TIME_POLICY = """
            INSERT INTO policy_sub (sub_id, block_policy_id, is_active, created_time, modified_time)
            VALUES (:subId, :policyId, :isActive, now(), now())
            """;

    private static final String SQL_UPDATE_MEMBER_APP_POLICY_ACTIVE = """
            UPDATE blocked_service_sub
            SET is_active = :isActive,
                modified_time = now()
            WHERE sub_id = :subId
              AND blocked_service_id = :policyId
            """;

    private static final String SQL_INSERT_MEMBER_APP_POLICY = """
            INSERT INTO blocked_service_sub (sub_id, blocked_service_id, is_active, created_time, modified_time)
            VALUES (:subId, :policyId, true, now(), now())
            """;

    private static final String SQL_BULK_DEACTIVATE_TIME_POLICIES_BY_IDS = """
            UPDATE policy_sub
            SET is_active = false,
                modified_time = now()
            WHERE policy_sub_id IN (:policySubIds)
              AND is_active = true
            """;

    private static final String SQL_BULK_DEACTIVATE_APP_POLICIES_BY_POLICY_ID = """
            UPDATE blocked_service_sub
            SET is_active = false,
                modified_time = now()
            WHERE blocked_service_id = :policyId
              AND is_active = true
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Set<Long> findExistingTimePolicyIds(Long familyId, Set<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return Set.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("policyIds", policyIds);

        List<Long> ids = jdbcTemplate.queryForList(SQL_FIND_EXISTING_TIME_POLICY_IDS, params, Long.class);
        return new HashSet<>(ids);
    }

    @Override
    public Set<Long> findExistingAppPolicyIds(Set<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return Set.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("policyIds", policyIds)
                .addValue("excludedPolicyCode", EXCLUDED_FAMILY_APP_POLICY_CODE);

        List<Long> ids = jdbcTemplate.queryForList(SQL_FIND_EXISTING_APP_POLICY_IDS, params, Long.class);
        return new HashSet<>(ids);
    }

    @Override
    public int updateMemberTimePolicyActive(Long subId, Long policyId, boolean isActive) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        return jdbcTemplate.update(SQL_UPDATE_MEMBER_TIME_POLICY_ACTIVE, params);
    }

    @Override
    public void insertMemberTimePolicy(Long subId, Long policyId, boolean isActive) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        jdbcTemplate.update(SQL_INSERT_MEMBER_TIME_POLICY, params);
    }

    @Override
    public int updateMemberAppPolicyActive(Long subId, Long policyId, boolean isActive) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId)
                .addValue("isActive", isActive);

        return jdbcTemplate.update(SQL_UPDATE_MEMBER_APP_POLICY_ACTIVE, params);
    }

    @Override
    public void insertMemberAppPolicy(Long subId, Long policyId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId)
                .addValue("policyId", policyId);

        jdbcTemplate.update(SQL_INSERT_MEMBER_APP_POLICY, params);
    }

    @Override
    public void bulkDeactivateTimePoliciesByIds(Set<Long> policySubIds) {
        if (policySubIds == null || policySubIds.isEmpty()) {
            return;
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("policySubIds", policySubIds);

        jdbcTemplate.update(SQL_BULK_DEACTIVATE_TIME_POLICIES_BY_IDS, params);
    }

    @Override
    public void bulkDeactivateAppPoliciesByPolicyId(Long policyId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("policyId", policyId);

        jdbcTemplate.update(SQL_BULK_DEACTIVATE_APP_POLICIES_BY_POLICY_ID, params);
    }
}
