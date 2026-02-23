package hotspot.admin.policy.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.appservice.infrastructure.entity.AppBlockedServiceEntity;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AppBlockedServiceRepositoryImpl implements AppBlockedServiceRepository {

    private final AppBlockedServiceJpaRepository appBlockedServiceJpaRepository;

    @Override
    public Page<AppBlockedService> findAll(Pageable pageable) {
        return appBlockedServiceJpaRepository.findAll(pageable)
                .map(hotspot.admin.appservice.infrastructure.entity.AppBlockedServiceEntity::entityToDomain);
    }

    @Override
    public int updateActiveById(Long policyId, Boolean isActive) {
        return appBlockedServiceJpaRepository.updateActiveById(policyId, isActive);
    }

    @Override
    public int softDeleteById(Long policyId) {
        return appBlockedServiceJpaRepository.softDeleteById(policyId);
    }

    @Override
    public Optional<AppBlockedService> findById(Long policyId) {
        return appBlockedServiceJpaRepository.findById(policyId)
                .map(AppBlockedServiceEntity::entityToDomain);
    }
}
