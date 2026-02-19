package hotspot.admin.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.family.infrastructure.entity.FamilyEntity;

public interface FamilyJpaRepository extends JpaRepository<FamilyEntity, Long> {
}
