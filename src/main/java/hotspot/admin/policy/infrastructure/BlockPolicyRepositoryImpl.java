package hotspot.admin.policy.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockPolicyRepositoryImpl implements BlockPolicyRepository {

    private final BlockPolicyJpaRepository blockPolicyJpaRepository;

    @Override
    public Page<BlockPolicy> findAll(Pageable pageable) {
        return blockPolicyJpaRepository.findAllByFamilyIdIsNull(pageable)
                .map(BlockPolicyEntity::entityToDomain);
    }

    @Override
    public BlockPolicy save(BlockPolicy blockPolicy) {
        BlockPolicyEntity saved = blockPolicyJpaRepository.save(BlockPolicyEntity.domainToEntity(blockPolicy));
        return saved.entityToDomain();
    }

    @Override
    public boolean existsTemplateByPolicyNameAndPolicyType(String policyName, PolicyType policyType) {
        return blockPolicyJpaRepository.existsByPolicyNameAndPolicyTypeAndFamilyIdIsNull(policyName, policyType);
    }

    @Override
    public int updateActiveById(Long policyId, Boolean isActive) {
        return blockPolicyJpaRepository.updateActiveById(policyId, isActive);
    }

    @Override
    public int softDeleteById(Long policyId) {
        return blockPolicyJpaRepository.softDeleteById(policyId);
    }

    @Override
    public Optional<BlockPolicy> findById(Long policyId) {
        return blockPolicyJpaRepository.findById(policyId)
                .map(BlockPolicyEntity::entityToDomain);
    }
}
