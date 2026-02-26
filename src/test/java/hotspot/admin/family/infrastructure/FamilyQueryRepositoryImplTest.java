package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.infrastructure.query.FamilyQueryRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyQueryRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("가족 목록 페이지 조회 시 row 매핑이 정상 동작한다")
    void findFamilyListSuccess() throws Exception {
        FamilyQueryRepositoryImpl familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
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
        FamilyQueryRepositoryImpl familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
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
        FamilyQueryRepositoryImpl familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
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
    @DisplayName("가족 목록 총 개수 조회 성공")
    void countFamilyListSuccess() {
        FamilyQueryRepositoryImpl familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(15L);

        long total = familyQueryRepository.countFamilyList();

        assertThat(total).isEqualTo(15L);
    }

    @Test
    @DisplayName("가족 목록 총 개수 null이면 0 반환")
    void countFamilyListNullThenZero() {
        FamilyQueryRepositoryImpl familyQueryRepository = new FamilyQueryRepositoryImpl(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);

        long total = familyQueryRepository.countFamilyList();

        assertThat(total).isZero();
    }
}
