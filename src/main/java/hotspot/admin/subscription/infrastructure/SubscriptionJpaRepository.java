package hotspot.admin.subscription.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SubscriptionEntity s
            SET s.isLocked = :isLocked
            WHERE s.subId = :subId
              AND EXISTS (
                  SELECT fs.familySubId
                  FROM FamilySubEntity fs
                  WHERE fs.family.familyId = :familyId
                    AND fs.subscription.subId = :subId
              )
            """)
    int updateIsLockedByFamilyIdAndSubId(
            @Param("familyId") Long familyId,
            @Param("subId") Long subId,
            @Param("isLocked") boolean isLocked
    );
}
