package hotspot.admin.family.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;

public interface FamilyJpaRepository extends JpaRepository<FamilyEntity, Long> {
    boolean existsByFamilyIdAndIsDeletedFalse(Long familyId);

    @Query("""
            SELECT f.priorityType
            FROM FamilyEntity f
            WHERE f.familyId = :familyId
              AND f.isDeleted = false
            """)
    Optional<PriorityType> findPriorityTypeByFamilyId(@Param("familyId") Long familyId);
}
