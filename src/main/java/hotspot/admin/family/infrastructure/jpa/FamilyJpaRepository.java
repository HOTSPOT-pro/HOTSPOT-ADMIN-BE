package hotspot.admin.family.infrastructure.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;

public interface FamilyJpaRepository extends JpaRepository<FamilyEntity, Long> {
    /** 삭제되지 않은 가족 존재 여부를 확인한다. */
    boolean existsByFamilyIdAndIsDeletedFalse(Long familyId);

    /** 가족 우선순위 유형(FIFO/PRIORITY)을 조회한다. */
    @Query("""
            SELECT f.priorityType
            FROM FamilyEntity f
            WHERE f.familyId = :familyId
              AND f.isDeleted = false
            """)
    Optional<PriorityType> findPriorityTypeByFamilyId(@Param("familyId") Long familyId);

    /** 가족 구성원 수/공유 데이터량을 함께 갱신한다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilyEntity f
            SET f.familyNum = :familyNum,
                f.familyDataAmount = :familyDataAmount
            WHERE f.familyId = :familyId
              AND f.isDeleted = false
            """)
    int updateFamilySummary(
            @Param("familyId") Long familyId,
            @Param("familyNum") Integer familyNum,
            @Param("familyDataAmount") Long familyDataAmount
    );
}
