package hotspot.admin.family.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.GetFamilyControlStatusService;
import hotspot.admin.family.controller.response.FamilyControlMemberItem;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyControlStatusServiceImpl implements GetFamilyControlStatusService {

    private static final long KB_PER_MB = 1024L;

    private final FamilyRepository familyRepository;

    @Transactional(readOnly = true)
    @Override
    public FamilyControlStatusResponse getFamilyControlStatus(Long familyId) {
        PriorityType priorityType = familyRepository.findFamilyPriorityType(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        List<FamilyControlMemberItem> members = familyRepository.findFamilyControlMembers(familyId).stream()
                .map(member -> toMemberItem(member, priorityType))
                .toList();

        return FamilyControlStatusResponse.builder()
                .priorityType(priorityType)
                .members(members)
                .build();
    }

    private FamilyControlMemberItem toMemberItem(FamilyControlMemberRow member, PriorityType priorityType) {
        return FamilyControlMemberItem.builder()
                .subId(member.subId())
                .memberName(member.memberName())
                .familyRole(member.familyRole())
                .blocked(Boolean.TRUE.equals(member.blocked()))
                .dataLimitMb(toMb(member.dataLimit()))
                .priorityOrder(resolvePriorityOrder(member.priority(), priorityType))
                .build();
    }

    private Integer resolvePriorityOrder(Integer priority, PriorityType priorityType) {
        if (priorityType == PriorityType.FIFO) {
            return -1;
        }
        return priority == null ? -1 : priority;
    }

    private Long toMb(Long dataLimit) {
        if (dataLimit == null) {
            return null;
        }
        return dataLimit / KB_PER_MB;
    }
}
