package hotspot.admin.usage.familyUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.admin.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.admin.usage.familyUsage.infrastructure.respository.FamilySubscriptionJdbcRepository;
import hotspot.admin.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFamilyUsageServiceImpl implements FindFamilyUsageService {

    private final FamilyUsageRepository findFamilyUsageRepository;
    private final FamilySubscriptionJdbcRepository familySubscriptionJdbcRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public FamilyUsageResponse findFamilyUsage(Long familyId) {

        List<FamilySubList> familySubList =
                familySubscriptionJdbcRepository.findByFamilyId(familyId);

        FamilyUsage familyUsage = findFamilyUsageRepository.findFamilyUsage(familyId, familySubList);

        LocalDateTime now = LocalDateTime.now(clock);

        return FamilyUsageMapper.toFamilyUsageResponse(familyUsage, familySubList, now);
    }
}
