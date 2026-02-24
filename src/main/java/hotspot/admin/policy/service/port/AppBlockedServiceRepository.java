package hotspot.admin.policy.service.port;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import hotspot.admin.appservice.domain.AppBlockedService;

public interface AppBlockedServiceRepository {

    Page<AppBlockedService> findAll(Pageable pageable);

    AppBlockedService save(AppBlockedService appBlockedService);
    boolean existsByBlockedServiceCode(String policyCode);

    int updateActiveById(Long policyId, Boolean isActive);
    int softDeleteById(Long policyId);

    Optional<AppBlockedService> findById(Long policyId);
}
