package hotspot.admin.policy.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockPolicyRepositoryImpl implements BlockPolicyRepository {

    private final BlockPolicyJpaRepository blockPolicyJpaRepository;

    @Override
    public Page<BlockPolicy> findAll(Pageable pageable) {
        return blockPolicyJpaRepository.findAll(pageable)
                .map(hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity::entityToDomain);
    }
}
