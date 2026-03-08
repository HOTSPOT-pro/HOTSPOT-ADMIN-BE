package hotspot.admin.policy.service.port;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyType;

public interface BlockPolicyRepository {

    Page<BlockPolicy> findAll(Pageable pageable);

    BlockPolicy save(BlockPolicy blockPolicy);
    boolean existsTemplateByPolicyNameAndPolicyType(String policyName, PolicyType policyType);

    List<BlockPolicy> findAllById(List<Long> idList);

    int updateActiveById(Long policyId, Boolean isActive);
    int softDeleteById(Long policyId);

    Optional<BlockPolicy> findById(Long policyId);
}
