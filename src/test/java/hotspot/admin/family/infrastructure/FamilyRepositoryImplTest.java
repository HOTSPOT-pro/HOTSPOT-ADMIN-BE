package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.service.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.dto.FamilyPolicyTimePolicyRow;

@ExtendWith(MockitoExtension.class)
class FamilyRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private FamilyRepositoryImpl familyRepository;

    @BeforeEach
    void setUp() {
        familyRepository = new FamilyRepositoryImpl(jdbcTemplate);
    }

    @Test
    @DisplayName("가족 목록 페이지 조회 시 row 매핑이 정상 동작한다")
    void findFamilyListSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyListItem> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("family_id")).thenReturn(31L);
                    when(rs.getString("representative_name")).thenReturn("김가족");
                    when(rs.getString("phone_number_enc")).thenReturn("encrypted-phone");
                    when(rs.getInt("member_count")).thenReturn(4);

                    FamilyListItem row = mapper.mapRow(rs, 0);
                    return List.of(row);
                });

        List<FamilyListItem> result = familyRepository.findFamilyList(20, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).familyId()).isEqualTo(31L);
        assertThat(result.get(0).representativeName()).isEqualTo("김가족");
        assertThat(result.get(0).phoneNumber()).isEqualTo("encrypted-phone");
        assertThat(result.get(0).memberCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("전화번호 해시로 가족 목록 조회 시 row 매핑이 정상 동작한다")
    void findFamilyByPhoneHashSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyListItem> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("family_id")).thenReturn(7L);
                    when(rs.getString("representative_name")).thenReturn("박대표");
                    when(rs.getString("phone_number_enc")).thenReturn("enc-phone");
                    when(rs.getInt("member_count")).thenReturn(2);

                    FamilyListItem row = mapper.mapRow(rs, 0);
                    return List.of(row);
                });

        Optional<FamilyListItem> result = familyRepository.findFamilyByPhoneHash("hashed-phone");

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(7L);
        assertThat(result.get().representativeName()).isEqualTo("박대표");
        assertThat(result.get().phoneNumber()).isEqualTo("enc-phone");
        assertThat(result.get().memberCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("가족 ID로 상세 상단 조회 시 row 매핑이 정상 동작한다")
    void findFamilyByIdSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyListItem> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("family_id")).thenReturn(9L);
                    when(rs.getString("representative_name")).thenReturn("이대표");
                    when(rs.getString("phone_number_enc")).thenReturn("enc-9999");
                    when(rs.getInt("member_count")).thenReturn(5);

                    FamilyListItem row = mapper.mapRow(rs, 0);
                    return List.of(row);
                });

        Optional<FamilyListItem> result = familyRepository.findFamilyById(9L);

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(9L);
        assertThat(result.get().representativeName()).isEqualTo("이대표");
        assertThat(result.get().phoneNumber()).isEqualTo("enc-9999");
        assertThat(result.get().memberCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("가족 존재 여부 조회 성공")
    void existsFamilyByIdSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(true);

        boolean exists = familyRepository.existsFamilyById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("가족 존재 여부 조회 결과 null이면 false")
    void existsFamilyByIdNullThenFalse() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(null);

        boolean exists = familyRepository.existsFamilyById(1L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("가족 우선순위 타입 조회 성공")
    void findFamilyPriorityTypeSuccess() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<PriorityType> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getString("priority_type")).thenReturn("FIFO");

                    return List.of(mapper.mapRow(rs, 0));
                });

        Optional<PriorityType> result = familyRepository.findFamilyPriorityType(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(PriorityType.FIFO);
    }

    @Test
    @DisplayName("가족 제어 기능 구성원 조회 시 row 매핑이 정상 동작한다")
    void findFamilyControlMembersSuccess() throws Exception {
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

        List<FamilyControlMemberRow> result = familyRepository.findFamilyControlMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(501L);
        assertThat(result.get(0).memberName()).isEqualTo("대표");
        assertThat(result.get(0).familyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(result.get(0).blocked()).isFalse();
        assertThat(result.get(0).dataLimit()).isEqualTo(2048L);
        assertThat(result.get(0).priority()).isEqualTo(1);
    }

    @Test
    @DisplayName("가족 정책 현황 구성원 조회 시 row 매핑이 정상 동작한다")
    void findFamilyPolicyMembersSuccess() throws Exception {
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

        List<FamilyPolicyMemberRow> result = familyRepository.findFamilyPolicyMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(100L);
        assertThat(result.get(0).memberName()).isEqualTo("대표");
        assertThat(result.get(0).familyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(result.get(0).blocked()).isTrue();
    }

    @Test
    @DisplayName("가족 정책 현황 시간대 정책 조회 시 row 매핑이 정상 동작한다")
    void findFamilyTimePoliciesSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyTimePolicyRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(101L);
                    when(rs.getString("policy_name")).thenReturn("야간 차단");

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyTimePolicyRow> result = familyRepository.findFamilyTimePolicies(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(101L);
        assertThat(result.get(0).policyName()).isEqualTo("야간 차단");
    }

    @Test
    @DisplayName("가족 정책 현황 차단 서비스 조회 시 row 매핑이 정상 동작한다")
    void findFamilyAppPoliciesSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyPolicyAppPolicyRow> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(102L);
                    when(rs.getString("blocked_service_name")).thenReturn("유튜브");

                    return List.of(mapper.mapRow(rs, 0));
                });

        List<FamilyPolicyAppPolicyRow> result = familyRepository.findFamilyAppPolicies(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(102L);
        assertThat(result.get(0).blockedServiceName()).isEqualTo("유튜브");
    }

    @Test
    @DisplayName("가족 정책 현황 통합 조회 시 row 매핑이 정상 동작한다")
    void findFamilyPolicyStatusRowsSuccess() throws Exception {
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

        List<FamilyPolicyStatusRow> result = familyRepository.findFamilyPolicyStatusRows(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(201L);
        assertThat(result.get(0).memberName()).isEqualTo("대표");
        assertThat(result.get(0).familyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(result.get(0).appliedTimePolicies()).containsExactly("야간 차단", "학습 시간");
        assertThat(result.get(0).appliedBlockedServicePolicies()).containsExactly("유튜브");
    }

    @Test
    @DisplayName("가족 목록 총 개수 조회 성공")
    void countFamilyListSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(15L);

        long total = familyRepository.countFamilyList();

        assertThat(total).isEqualTo(15L);
    }

    @Test
    @DisplayName("가족 목록 총 개수 null이면 0 반환")
    void countFamilyListNullThenZero() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);

        long total = familyRepository.countFamilyList();

        assertThat(total).isZero();
    }

    @Test
    @DisplayName("가족 요청 목록 조회 시 row 매핑이 정상 동작한다")
    void findFamilyRequestListSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<FamilyRequestListItem> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("family_apply_id")).thenReturn(13L);
                    when(rs.getLong("family_id")).thenReturn(2L);
                    when(rs.getString("requester_name")).thenReturn("가족대표");
                    when(rs.getString("requester_phone_enc")).thenReturn("enc-1");
                    when(rs.getString("target_name")).thenReturn("추가대상");
                    when(rs.getString("target_phone_enc")).thenReturn("enc-2");
                    when(rs.getString("target_family_role")).thenReturn("CHILD");
                    when(rs.getString("doc_url")).thenReturn("https://doc.example/1");
                    when(rs.getTimestamp("created_time"))
                            .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 2, 24, 9, 30)));

                    FamilyRequestListItem row = mapper.mapRow(rs, 0);
                    return List.of(row);
                });

        List<FamilyRequestListItem> result = familyRepository.findFamilyRequestList(
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                20,
                0
        );

        assertThat(result).hasSize(1);
        FamilyRequestListItem item = result.get(0);
        assertThat(item.requestId()).isEqualTo(13L);
        assertThat(item.familyId()).isEqualTo(2L);
        assertThat(item.requesterName()).isEqualTo("가족대표");
        assertThat(item.targetName()).isEqualTo("추가대상");
        assertThat(item.targetFamilyRole()).isEqualTo(FamilyRole.CHILD);
        assertThat(item.requestedAt()).isEqualTo(LocalDateTime.of(2026, 2, 24, 9, 30));
    }

    @Test
    @DisplayName("가족 요청 목록 개수 조회 성공")
    void countFamilyRequestListSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(12L);

        long result = familyRepository.countFamilyRequestList(ApplyType.REMOVE, FamilyApplyStatus.APPROVED);

        assertThat(result).isEqualTo(12L);
    }

    @Test
    @DisplayName("가족 요청 목록 개수 조회 결과 null이면 0을 반환한다")
    void countFamilyRequestListNullThenZero() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);

        long result = familyRepository.countFamilyRequestList(ApplyType.ADD, FamilyApplyStatus.REJECTED);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("대기중 요청 상태 업데이트 성공")
    void updateFamilyRequestStatusSuccess() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        int updated = familyRepository.updateFamilyRequestStatus(
                1L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        );

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("요청 존재 여부 조회 성공")
    void existsFamilyRequestSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(true);

        boolean exists = familyRepository.existsFamilyRequest(10L, ApplyType.REMOVE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("요청 존재 여부 조회 null이면 false")
    void existsFamilyRequestNullThenFalse() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(null);

        boolean exists = familyRepository.existsFamilyRequest(11L, ApplyType.ADD);

        assertThat(exists).isFalse();
    }
}
