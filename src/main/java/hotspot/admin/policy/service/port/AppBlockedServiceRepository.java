package hotspot.admin.policy.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import hotspot.admin.appservice.domain.AppBlockedService;

public interface AppBlockedServiceRepository {

    Page<AppBlockedService> findAll(Pageable pageable);

    int updateActiveById(Long policyId, Boolean isActive);
    int softDeleteById(Long policyId);

    Optional<AppBlockedService> findById(Long policyId);
}
