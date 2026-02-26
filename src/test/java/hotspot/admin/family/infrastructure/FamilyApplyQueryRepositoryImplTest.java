package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.query.FamilyApplyQueryRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyApplyQueryRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("가족 요청 목록 조회 시 row 매핑이 정상 동작한다")
    void findFamilyRequestListSuccess() throws Exception {
        FamilyApplyQueryRepositoryImpl familyApplyQueryRepository = new FamilyApplyQueryRepositoryImpl(jdbcTemplate);
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
        FamilyApplyQueryRepositoryImpl familyApplyQueryRepository = new FamilyApplyQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(12L);

        long result = familyApplyQueryRepository.countFamilyRequestList(ApplyType.REMOVE, FamilyApplyStatus.APPROVED);

        assertThat(result).isEqualTo(12L);
    }
}
