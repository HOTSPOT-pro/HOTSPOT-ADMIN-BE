package hotspot.admin.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {
}
