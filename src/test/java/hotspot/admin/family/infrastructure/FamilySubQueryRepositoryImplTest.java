package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.family.infrastructure.query.FamilySubQueryRepositoryImpl;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;

@ExtendWith(MockitoExtension.class)
class FamilySubQueryRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("가족 제어 기능 구성원 조회 시 row 매핑이 정상 동작한다")
    void findFamilyControlMembersSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyControlMemberRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(501L);
                    when(rs.getString("member_name")).thenReturn("대표");
                    when(rs.getString("family_role")).thenReturn("OWNER");
                    when(rs.getBoolean("blocked")).thenReturn(false);
                    when(rs.getObject("data_limit", Long.class)).thenReturn(2048L);
                    when(rs.getObject("priority", Integer.class)).thenReturn(1);

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyControlMemberRow> result = familySubRepository.findFamilyControlMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(501L);
    }

    @Test
    @DisplayName("가족 정책 현황 구성원 조회 시 row 매핑이 정상 동작한다")
    void findFamilyPolicyMembersSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyMemberRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(100L);
                    when(rs.getString("member_name")).thenReturn("대표");
                    when(rs.getString("phone_number_enc")).thenReturn("enc-phone");
                    when(rs.getString("family_role")).thenReturn("OWNER");
                    when(rs.getBoolean("blocked")).thenReturn(true);

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyMemberRow> result = familySubRepository.findFamilyPolicyMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("가족 정책 현황 시간대 정책 조회 시 row 매핑이 정상 동작한다")
    void findFamilyTimePoliciesSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyTimePolicyRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    Timestamp modifiedTime = Timestamp.valueOf("2026-03-09 10:30:00");

                    when(rs.getLong("policy_sub_id")).thenReturn(1001L);
                    when(rs.getLong("sub_id")).thenReturn(101L);
                    when(rs.getLong("policy_id")).thenReturn(1L);
                    when(rs.getString("policy_name")).thenReturn("야간 차단");
                    when(rs.getTimestamp("modified_time")).thenReturn(modifiedTime);

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyTimePolicyRow> result = familySubRepository.findFamilyTimePolicies(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("가족 정책 현황 차단 서비스 조회 시 row 매핑이 정상 동작한다")
    void findFamilyAppPoliciesSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyAppPolicyRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(102L);
                    when(rs.getLong("policy_id")).thenReturn(11L);
                    when(rs.getString("blocked_service_name")).thenReturn("유튜브");

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyAppPolicyRow> result = familySubRepository.findFamilyAppPolicies(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("가족 정책 현황 시간 정책 옵션 조회 시 row 매핑이 정상 동작한다")
    void findFamilyTimePolicyOptionsSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyTimeOptionRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("policy_id")).thenReturn(201L);
                    when(rs.getString("policy_name")).thenReturn("야간 차단");
                    when(rs.getString("policy_description")).thenReturn("매일 야간 차단");
                    when(rs.getString("policy_type")).thenReturn("SCHEDULED");
                    when(rs.getString("policy_snapshot_json")).thenReturn(
                            "{\"days\":[\"MON\",\"TUE\"],\"startTime\":\"22:00\",\"endTime\":\"07:00\"}"
                    );

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyTimeOptionRow> result = familySubRepository.findFamilyTimePolicyOptions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).policyName()).isEqualTo("야간 차단");
    }

    @Test
    @DisplayName("가족 정책 현황 앱 정책 옵션 조회 시 row 매핑이 정상 동작한다")
    void findAllAppPolicyOptionsSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyAppOptionRow> mapper = invocation.getArgument(1);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("policy_id")).thenReturn(301L);
                    when(rs.getString("blocked_service_name")).thenReturn("유튜브");

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyAppOptionRow> result = familySubRepository.findAllAppPolicyOptions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).policyName()).isEqualTo("유튜브");
    }

    @Test
    @DisplayName("가족 정책 현황 통합 조회 시 row 매핑이 정상 동작한다")
    void findFamilyPolicyStatusRowsSuccess() throws Exception {
        FamilySubQueryRepositoryImpl familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyStatusRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    Array timeArray = org.mockito.Mockito.mock(Array.class);
                    Array appArray = org.mockito.Mockito.mock(Array.class);

                    when(rs.getLong("sub_id")).thenReturn(201L);
                    when(rs.getString("member_name")).thenReturn("대표");
                    when(rs.getString("phone_number_enc")).thenReturn("enc-phone");
                    when(rs.getString("family_role")).thenReturn("OWNER");
                    when(rs.getBoolean("blocked")).thenReturn(true);
                    when(rs.getArray("time_policy_names")).thenReturn(timeArray);
                    when(rs.getArray("app_policy_names")).thenReturn(appArray);
                    when(timeArray.getArray()).thenReturn(new String[]{"야간 차단", "학습 시간"});
                    when(appArray.getArray()).thenReturn(new String[]{"유튜브"});

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyStatusRow> result = familySubRepository.findFamilyPolicyStatusRows(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).appliedTimePolicies()).containsExactly("야간 차단", "학습 시간");
    }
}
