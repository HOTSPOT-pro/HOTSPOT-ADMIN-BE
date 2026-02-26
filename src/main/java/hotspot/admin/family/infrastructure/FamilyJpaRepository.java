package hotspot.admin.family.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
