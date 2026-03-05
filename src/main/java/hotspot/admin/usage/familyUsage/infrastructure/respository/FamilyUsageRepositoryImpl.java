package hotspot.admin.usage.familyUsage.infrastructure.respository;

import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FamilyUsageRepositoryImpl implements FamilyUsageRepository {

    private final FamilyUsageRedisRepository redisRepository;

    @Override
    public FamilyUsage findFamilyUsage(
            Long familyId,
            List<FamilySubList> familySubList
    ) {

        List<Long> subIds = familySubList.stream()
                .map(FamilySubList::subId)
                .toList();

        return redisRepository.findFamilyAndSubData(familyId, subIds);
    }
}
