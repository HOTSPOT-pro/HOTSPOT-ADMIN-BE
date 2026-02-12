package hotspot.admin.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.auth.infrastructure.entity.AdminEntity;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByAdminCode(String adminCode);
}
