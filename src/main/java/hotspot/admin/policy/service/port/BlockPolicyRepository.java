package hotspot.admin.policy.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import hotspot.admin.policy.domain.BlockPolicy;

public interface BlockPolicyRepository {

    Page<BlockPolicy> findAll(Pageable pageable);
}
