package hotspot.admin.policy.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import hotspot.admin.policy.domain.BlockPolicy;

public interface BlockPolicyRepository {

    Page<BlockPolicy> findAll(Pageable pageable);

    int updateActiveById(Long policyId, Boolean isActive);
    int softDeleteById(Long policyId);

    Optional<BlockPolicy> findById(Long policyId);
}
