package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;

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
    @DisplayName("가족 목록 슬라이스 조회 시 row 매핑이 정상 동작한다")
    void findFamilySliceSuccess() throws Exception {
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

        List<FamilyListItem> result = familyRepository.findFamilySlice(31, 30L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).familyId()).isEqualTo(31L);
        assertThat(result.get(0).representativeName()).isEqualTo("김가족");
        assertThat(result.get(0).phoneNumber()).isEqualTo("encrypted-phone");
        assertThat(result.get(0).memberCount()).isEqualTo(4);
    }
}
