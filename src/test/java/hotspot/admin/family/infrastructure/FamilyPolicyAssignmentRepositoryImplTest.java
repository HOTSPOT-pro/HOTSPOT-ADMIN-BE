package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.family.infrastructure.query.FamilyPolicyAssignmentRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyPolicyAssignmentRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private FamilyPolicyAssignmentRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new FamilyPolicyAssignmentRepositoryImpl(jdbcTemplate);
    }

    @Test
    @DisplayName("가족 시간 정책 ID 조회 성공")
    void findExistingTimePolicyIdsSuccess() {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(101L, 102L));

        Set<Long> result = repository.findExistingTimePolicyIds(1L, Set.of(101L, 102L, 103L));

        assertThat(result).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    @DisplayName("가족 시간 정책 ID 조회 시 빈 요청이면 빈 Set 반환")
    void findExistingTimePolicyIdsWithEmptyRequest() {
        Set<Long> result = repository.findExistingTimePolicyIds(1L, Set.of());

        assertThat(result).isEmpty();
        verify(jdbcTemplate, never()).queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
    }

    @Test
    @DisplayName("앱 정책 ID 조회 성공")
    void findExistingAppPolicyIdsSuccess() {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(201L, 202L));

        Set<Long> result = repository.findExistingAppPolicyIds(Set.of(201L, 202L, 203L));

        assertThat(result).containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    @DisplayName("구성원 시간 정책 활성화 업데이트 성공")
    void updateMemberTimePolicyActiveSuccess() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        int updated = repository.updateMemberTimePolicyActive(10L, 101L, true);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("구성원 시간 정책 삽입 호출 성공")
    void insertMemberTimePolicySuccess() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.insertMemberTimePolicy(10L, 101L, true);

        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("구성원 앱 정책 활성 상태 업데이트 성공")
    void updateMemberAppPolicyActiveSuccess() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        int updated = repository.updateMemberAppPolicyActive(10L, 201L, true);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("구성원 앱 정책 삽입 호출 성공")
    void insertMemberAppPolicySuccess() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.insertMemberAppPolicy(10L, 201L);

        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }
}
