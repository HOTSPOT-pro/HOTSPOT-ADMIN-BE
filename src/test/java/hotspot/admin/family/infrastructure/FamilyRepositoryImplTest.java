package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
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
}
