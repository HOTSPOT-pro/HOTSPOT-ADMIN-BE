package hotspot.admin.policy.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import hotspot.admin.appservice.domain.AppBlockedService;

public interface AppBlockedServiceRepository {

    Page<AppBlockedService> findAll(Pageable pageable);
}
