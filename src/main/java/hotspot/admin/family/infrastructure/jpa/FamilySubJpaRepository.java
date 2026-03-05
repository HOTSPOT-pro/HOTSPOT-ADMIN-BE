package hotspot.admin.family.infrastructure.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.entity.FamilySubEntity;

import java.util.List;

public interface FamilySubJpaRepository extends JpaRepository<FamilySubEntity, Long> {
    boolean existsByFamilyFamilyIdAndSubscriptionSubId(Long familyId, Long subId);


    @EntityGraph(attributePaths = {
            "family",
            "subscription",
            "subscription.member"
    })
    List<FamilySubEntity> findByFamilyFamilyId(Long familyId);

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
            SET fs.dataLimit = :dataLimit
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.subId = :subId
            """)
    int updateDataLimitByFamilyIdAndSubId(
            @Param("familyId") Long familyId,
            @Param("subId") Long subId,
            @Param("dataLimit") Long dataLimit
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilySubEntity fs
            SET fs.priority = :priority
            WHERE fs.family.familyId = :familyId
            """)
    int updatePriorityByFamilyId(@Param("familyId") Long familyId, @Param("priority") Integer priority);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilySubEntity fs
            SET fs.priority = :priority
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.subId = :subId
            """)
    int updatePriorityByFamilyIdAndSubId(
            @Param("familyId") Long familyId,
            @Param("subId") Long subId,
            @Param("priority") Integer priority
    );

    @Query("""
            SELECT fs.familyRole
            FROM FamilySubEntity fs
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.subId = :subId
            """)
    java.util.Optional<FamilyRole> findFamilyRoleByFamilyIdAndSubId(
            @Param("familyId") Long familyId,
            @Param("subId") Long subId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilySubEntity fs
            SET fs.familyRole = :familyRole
            WHERE fs.family.familyId = :familyId
              AND fs.subscription.subId = :subId
            """)
    int updateFamilyRoleByFamilyIdAndSubId(
            @Param("familyId") Long familyId,
            @Param("subId") Long subId,
            @Param("familyRole") FamilyRole familyRole
    );
}
