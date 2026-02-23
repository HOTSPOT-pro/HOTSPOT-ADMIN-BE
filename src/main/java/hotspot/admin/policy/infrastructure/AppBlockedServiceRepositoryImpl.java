package hotspot.admin.policy.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import hotspot.admin.appservice.domain.AppBlockedService;
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
}
