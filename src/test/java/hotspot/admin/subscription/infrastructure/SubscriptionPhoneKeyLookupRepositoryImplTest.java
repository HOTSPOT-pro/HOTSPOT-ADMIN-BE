package hotspot.admin.subscription.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import hotspot.admin.subscription.service.port.dto.SubscriptionPhoneKeyInfo;

@ExtendWith(MockitoExtension.class)
class SubscriptionPhoneKeyLookupRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("subId로 전화번호 키 정보를 조회한다")
    void findPhoneKeyInfoBySubId() throws Exception {
        SubscriptionPhoneKeyLookupRepositoryImpl repository =
                new SubscriptionPhoneKeyLookupRepositoryImpl(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<SubscriptionPhoneKeyInfo> mapper = invocation.getArgument(2);

                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getLong("sub_id")).thenReturn(101L);
                    when(rs.getInt("bucket_id")).thenReturn(101);
                    when(rs.getInt("key_version")).thenReturn(3);
                    when(rs.getString("encrypted_dek")).thenReturn("wrapped-dek");
                    when(rs.getString("kek_key_id")).thenReturn("kek-arn");
                    when(rs.getString("status")).thenReturn("active");

                    return List.of(mapper.mapRow(rs, 0));
                });

        SubscriptionPhoneKeyInfo result = repository.findPhoneKeyInfoBySubId(101L).orElseThrow();

        assertThat(result.subId()).isEqualTo(101L);
        assertThat(result.bucketId()).isEqualTo(101);
        assertThat(result.keyVersion()).isEqualTo(3);
        assertThat(result.encryptedDek()).isEqualTo("wrapped-dek");
        assertThat(result.kekKeyId()).isEqualTo("kek-arn");
        assertThat(result.status()).isEqualTo("active");
    }

    @Test
    @DisplayName("조회 결과가 없으면 empty를 반환한다")
    void findPhoneKeyInfoBySubIdEmpty() {
        SubscriptionPhoneKeyLookupRepositoryImpl repository =
                new SubscriptionPhoneKeyLookupRepositoryImpl(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        assertThat(repository.findPhoneKeyInfoBySubId(999L)).isEmpty();
    }
}
