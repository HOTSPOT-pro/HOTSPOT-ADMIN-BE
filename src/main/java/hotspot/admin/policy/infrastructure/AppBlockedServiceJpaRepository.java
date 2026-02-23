package hotspot.admin.policy.infrastructure;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.appservice.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE app_blocked_service
                    SET is_active = :isActive,
                        modified_time = now()
                    WHERE app_blocked_service_id = :policyId
                      AND is_deleted = false
                    """,
            nativeQuery = true
    )
    int updateActiveById(@Param("policyId") Long policyId, @Param("isActive") Boolean isActive);
}
