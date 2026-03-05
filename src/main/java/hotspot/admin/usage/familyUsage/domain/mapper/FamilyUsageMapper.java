package hotspot.admin.usage.familyUsage.domain.mapper;

import java.time.LocalDateTime;
import java.util.List;

import hotspot.admin.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.admin.usage.familyUsage.domain.FamilySubUsage;
import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;

public class FamilyUsageMapper {

    public static FamilyUsageResponse toFamilyUsageResponse(
            FamilyUsage usage,
            List<FamilySubList> subs,
            LocalDateTime now
    ) {

        List<FamilyUsageResponse.FamilySubUsageResponse> subResponses =
                subs.stream()
                        .map(sub -> toFamilySubUsageResponse(usage, sub))
                        .toList();

        return new FamilyUsageResponse(
                now,
                usage.familyLimitGb(),
                usage.familyUsedGb(),
                usage.familyRemainGb(),
                usage.familyUsagePercent(),
                subResponses
        );
    }

    private static FamilyUsageResponse.FamilySubUsageResponse toFamilySubUsageResponse(
            FamilyUsage usage,
            FamilySubList sub
    ) {

        FamilySubUsage familySubUsage = usage.getSubOrZero(sub.subId());

        return new FamilyUsageResponse.FamilySubUsageResponse(
                sub.subId(),
                sub.subName(),
                familySubUsage.limitGb(),
                familySubUsage.familyUsedGb(),
                familySubUsage.remainGb(),
                familySubUsage.usagePercent()
        );
    }
}
