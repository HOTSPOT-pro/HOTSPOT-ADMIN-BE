package hotspot.admin.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.family.infrastructure.entity.FamilySubEntity;

public interface FamilySubJpaRepository extends JpaRepository<FamilySubEntity, Long> {
}
