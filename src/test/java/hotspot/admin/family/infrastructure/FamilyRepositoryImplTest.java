package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyRepositoryImpl;
import hotspot.admin.family.infrastructure.jpa.FamilyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyRepositoryJpaImpl;
import hotspot.admin.family.infrastructure.jpa.FamilySubJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilySubJpaRepositoryImpl;
import hotspot.admin.family.infrastructure.query.FamilyApplyQueryRepositoryImpl;
import hotspot.admin.family.infrastructure.query.FamilyQueryRepositoryImpl;
import hotspot.admin.family.infrastructure.query.FamilySubQueryRepositoryImpl;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;
import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.service.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.subscription.infrastructure.SubscriptionJpaRepository;
import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class FamilyRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private FamilyJpaRepository familyJpaRepository;

    @Mock
    private FamilyApplyJpaRepository familyApplyJpaRepository;

    @Mock
    private FamilySubJpaRepository familySubJpaRepository;

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    private FamilyQueryRepositoryImpl familyQueryRepository;
    private FamilyRepositoryJpaImpl familyRepository;
    private FamilySubQueryRepositoryImpl familySubRepository;
    private FamilyApplyQueryRepositoryImpl familyApplyQueryRepository;
    private FamilyApplyRepositoryImpl familyApplyRepository;
    private FamilySubJpaRepositoryImpl familySubJpaRepositoryImpl;

    @BeforeEach
    void setUp() {
        familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
        familyRepository = new FamilyRepositoryJpaImpl(familyJpaRepository);
        familySubRepository = new FamilySubQueryRepositoryImpl(jdbcTemplate);
        familyApplyQueryRepository = new FamilyApplyQueryRepositoryImpl(jdbcTemplate);
        familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);
        familySubJpaRepositoryImpl = new FamilySubJpaRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
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

        List<FamilyListItem> result = familyQueryRepository.findFamilyList(20, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).familyId()).isEqualTo(31L);
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

        Optional<FamilyListItem> result = familyQueryRepository.findFamilyByPhoneHash("hashed-phone");

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(7L);
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

        Optional<FamilyListItem> result = familyQueryRepository.findFamilyById(9L);

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("가족 존재 여부 조회 성공")
    void existsFamilyByIdSuccess() {
        when(familyJpaRepository.existsByFamilyIdAndIsDeletedFalse(1L)).thenReturn(true);

        boolean exists = familyRepository.existsFamilyById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("가족 우선순위 타입 조회 성공")
    void findFamilyPriorityTypeSuccess() {
        when(familyJpaRepository.findPriorityTypeByFamilyId(1L)).thenReturn(Optional.of(PriorityType.FIFO));

        Optional<PriorityType> result = familyRepository.findFamilyPriorityType(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(PriorityType.FIFO);
    }

    @Test
    @DisplayName("가족 요약 정보 업데이트 성공")
    void updateFamilySummarySuccess() {
        when(familyJpaRepository.updateFamilySummary(1L, 4, 20971520L)).thenReturn(1);

        int updated = familyRepository.updateFamilySummary(1L, 4, 20971520L);

        assertThat(updated).isEqualTo(1);
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

        List<FamilyControlMemberRow> result = familySubRepository.findFamilyControlMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(501L);
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

        List<FamilyPolicyMemberRow> result = familySubRepository.findFamilyPolicyMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(100L);
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

        List<FamilyPolicyTimePolicyRow> result = familySubRepository.findFamilyTimePolicies(1L);

        assertThat(result).hasSize(1);
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

        List<FamilyPolicyAppPolicyRow> result = familySubRepository.findFamilyAppPolicies(1L);

        assertThat(result).hasSize(1);
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

        List<FamilyPolicyStatusRow> result = familySubRepository.findFamilyPolicyStatusRows(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).appliedTimePolicies()).containsExactly("야간 차단", "학습 시간");
    }

    @Test
    @DisplayName("가족 목록 총 개수 조회 성공")
    void countFamilyListSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(15L);

        long total = familyQueryRepository.countFamilyList();

        assertThat(total).isEqualTo(15L);
    }

    @Test
    @DisplayName("가족 목록 총 개수 null이면 0 반환")
    void countFamilyListNullThenZero() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);

        long total = familyQueryRepository.countFamilyList();

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

        List<FamilyRequestListItem> result = familyApplyQueryRepository.findFamilyRequestList(
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                20,
                0
        );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("가족 요청 목록 개수 조회 성공")
    void countFamilyRequestListSuccess() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(12L);

        long result = familyApplyQueryRepository.countFamilyRequestList(ApplyType.REMOVE, FamilyApplyStatus.APPROVED);

        assertThat(result).isEqualTo(12L);
    }

    @Test
    @DisplayName("요청 상태 업데이트 성공")
    void updateFamilyRequestStatusSuccess() {
        when(familyApplyJpaRepository.updateStatusByIdAndTypeAndCurrentStatus(
                1L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        )).thenReturn(1);

        int updated = familyApplyRepository.updateFamilyRequestStatus(
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
        when(familyApplyJpaRepository.existsByFamilyApplyIdAndApplyType(10L, ApplyType.REMOVE)).thenReturn(true);

        boolean exists = familyApplyRepository.existsFamilyRequest(10L, ApplyType.REMOVE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("요청 승인 후처리 정보 조회 성공")
    void findAddApprovalInfoSuccess() {
        FamilyEntity family = FamilyEntity.builder()
                .familyId(3L)
                .build();
        SubscriptionEntity targetSubscription = SubscriptionEntity.builder()
                .subId(10L)
                .build();
        FamilyApplyEntity entity = FamilyApplyEntity.builder()
                .familyApplyId(1L)
                .family(family)
                .targetSubscription(targetSubscription)
                .targetFamilyRole(FamilyRole.CHILD)
                .applyType(ApplyType.ADD)
                .build();

        when(familyApplyJpaRepository.findByFamilyApplyIdAndApplyType(1L, ApplyType.ADD))
                .thenReturn(Optional.of(entity));

        Optional<FamilyAddApprovalInfo> result = familyApplyRepository.findAddApprovalInfo(1L);

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(3L);
        assertThat(result.get().targetSubId()).isEqualTo(10L);
        assertThat(result.get().targetFamilyRole()).isEqualTo(FamilyRole.CHILD);
    }

    @Test
    @DisplayName("family_sub 존재 여부 조회 성공")
    void existsFamilySubSuccess() {
        when(familySubJpaRepository.existsByFamilyFamilyIdAndSubscriptionSubId(1L, 2L))
                .thenReturn(true);

        boolean exists = familySubJpaRepositoryImpl.existsFamilySub(1L, 2L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("family_sub 최대 우선순위 조회 성공")
    void findMaxPrioritySuccess() {
        when(familySubJpaRepository.findMaxPriorityByFamilyId(1L)).thenReturn(7);

        int max = familySubJpaRepositoryImpl.findMaxPriority(1L);

        assertThat(max).isEqualTo(7);
    }

    @Test
    @DisplayName("family_sub 최대 우선순위가 null이면 0")
    void findMaxPriorityNullThenZero() {
        when(familySubJpaRepository.findMaxPriorityByFamilyId(1L)).thenReturn(null);

        int max = familySubJpaRepositoryImpl.findMaxPriority(1L);

        assertThat(max).isZero();
    }

    @Test
    @DisplayName("family_sub 저장 성공")
    void saveFamilySubSuccess() {
        FamilyEntity family = FamilyEntity.builder().familyId(3L).build();
        SubscriptionEntity subscription = SubscriptionEntity.builder().subId(100L).build();
        when(familyJpaRepository.getReferenceById(3L)).thenReturn(family);
        when(subscriptionJpaRepository.getReferenceById(100L)).thenReturn(subscription);

        familySubJpaRepositoryImpl.saveFamilySub(3L, 100L, FamilyRole.CHILD, 2, 0L);

        verify(familySubJpaRepository).save(any());
    }

    @Test
    @DisplayName("활성 구성원 수 조회 성공")
    void countActiveMembersSuccess() {
        when(familySubJpaRepository.countActiveMembersByFamilyId(3L)).thenReturn(4);

        int count = familySubJpaRepositoryImpl.countActiveMembers(3L);

        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("family_sub 데이터 한도 업데이트 성공")
    void updateDataLimitSuccess() {
        when(familySubJpaRepository.updateDataLimitByFamilyId(3L, 1024L)).thenReturn(3);

        int updated = familySubJpaRepositoryImpl.updateDataLimit(3L, 1024L);

        assertThat(updated).isEqualTo(3);
    }

    @Test
    @DisplayName("family_sub 우선순위 업데이트 성공")
    void updatePrioritySuccess() {
        when(familySubJpaRepository.updatePriorityByFamilyId(3L, -1)).thenReturn(3);

        int updated = familySubJpaRepositoryImpl.updatePriority(3L, -1);

        assertThat(updated).isEqualTo(3);
    }
}
