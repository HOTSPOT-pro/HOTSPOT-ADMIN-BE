package hotspot.admin.family.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.infrastructure.entity.FamilySubEntity;

public interface FamilySubJpaRepository extends JpaRepository<FamilySubEntity, Long> {
    boolean existsByFamilyFamilyIdAndSubscriptionSubId(Long familyId, Long subId);

    @Query("""
            SELECT COALESCE(MAX(fs.priority), 0)
            FROM FamilySubEntity fs
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.isDeleted = false
            """)
    Integer findMaxPriorityByFamilyId(@Param("familyId") Long familyId);

    @Query("""
            SELECT COUNT(fs)
            FROM FamilySubEntity fs
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.isDeleted = false
            """)
    int countActiveMembersByFamilyId(@Param("familyId") Long familyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilySubEntity fs
            SET fs.dataLimit = :dataLimit
            WHERE fs.family.familyId = :familyId
            """)
    int updateDataLimitByFamilyId(@Param("familyId") Long familyId, @Param("dataLimit") Long dataLimit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilySubEntity fs
            SET fs.priority = :priority
            WHERE fs.family.familyId = :familyId
            """)
    int updatePriorityByFamilyId(@Param("familyId") Long familyId, @Param("priority") Integer priority);
}
