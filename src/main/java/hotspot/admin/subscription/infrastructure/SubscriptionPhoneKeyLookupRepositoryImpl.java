package hotspot.admin.subscription.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.subscription.service.port.SubscriptionPhoneKeyLookupRepository;
import hotspot.admin.subscription.service.port.dto.SubscriptionPhoneKeyInfo;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionPhoneKeyLookupRepositoryImpl implements SubscriptionPhoneKeyLookupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<SubscriptionPhoneKeyInfo> findPhoneKeyInfoBySubId(Long subId) {
        String sql = """
                SELECT
                    s.sub_id,
                    s.phone_key_bucket_id AS bucket_id,
                    s.phone_key_version AS key_version,
                    sk.encrypted_dek,
                    sk.kek_key_id,
                    sk.status
                FROM subscription s
                JOIN subscription_key sk
                  ON sk.bucket_id = s.phone_key_bucket_id
                 AND sk.key_version = s.phone_key_version
                WHERE s.sub_id = :subId
                  AND s.is_deleted = false
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", subId);

        List<SubscriptionPhoneKeyInfo> result = jdbcTemplate.query(sql, params, (rs, rowNum) ->
                SubscriptionPhoneKeyInfo.builder()
                        .subId(rs.getLong("sub_id"))
                        .bucketId(rs.getInt("bucket_id"))
                        .keyVersion(rs.getInt("key_version"))
                        .encryptedDek(rs.getString("encrypted_dek"))
                        .kekKeyId(rs.getString("kek_key_id"))
                        .status(rs.getString("status"))
                        .build());

        return result.stream().findFirst();
    }
}
