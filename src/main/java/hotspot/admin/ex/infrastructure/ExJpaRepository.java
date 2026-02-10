package hotspot.admin.ex.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.ex.infrastructure.entity.ExEntity;

public interface ExJpaRepository extends JpaRepository<ExEntity, Long> {}
