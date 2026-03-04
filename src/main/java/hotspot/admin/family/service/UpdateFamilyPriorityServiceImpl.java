package hotspot.admin.family.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.UpdateFamilyPriorityTypeService;
import hotspot.admin.family.controller.request.MemberPriorityRequest;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateFamilyPriorityServiceImpl implements UpdateFamilyPriorityTypeService {

    private static final int UNUSED_PRIORITY_ORDER = -1;

    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;
    private final FamilySubQueryRepository familySubQueryRepository;

    /** 가족 우선순위 유형을 변경하고 요청값에 맞춰 구성원 우선순위를 동기화한다. */
    @Transactional
    @Override
    public void updatePriorityType(
            Long familyId,
            PriorityType priorityType,
            List<MemberPriorityRequest> memberPriorities
    ) {
        int updated = familyRepository.updateFamilyPriorityType(familyId, priorityType);
        if (updated == 0) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }

        if (priorityType == PriorityType.FIFO) {
            familySubRepository.updatePriority(familyId, UNUSED_PRIORITY_ORDER);
            return;
        }

        updatePriorityByRequest(familyId, memberPriorities);
    }

    /** PRIORITY 전환 시 요청한 구성원 우선순위 정보를 검증 후 반영한다. */
    private void updatePriorityByRequest(Long familyId, List<MemberPriorityRequest> memberPriorities) {
        if (memberPriorities == null || memberPriorities.isEmpty()) {
            throw new ApplicationException(FamilyErrorCode.MISSING_PRIORITY_VALUES);
        }

        List<FamilyControlMemberRow> members = familySubQueryRepository.findFamilyControlMembers(familyId);
        Map<Long, Integer> priorityMap = buildPriorityMap(memberPriorities);

        validateMemberCoverage(members, priorityMap);
        validatePriorityValues(priorityMap);

        List<MemberPriorityRequest> sorted = memberPriorities.stream()
                .sorted(Comparator.comparingInt(MemberPriorityRequest::priority))
                .toList();

        for (MemberPriorityRequest memberPriority : sorted) {
            familySubRepository.updateMemberPriority(
                    familyId,
                    memberPriority.subId(),
                    memberPriority.priority()
            );
        }
    }

    private Map<Long, Integer> buildPriorityMap(List<MemberPriorityRequest> memberPriorities) {
        Map<Long, Integer> priorityMap = new HashMap<>();
        for (MemberPriorityRequest memberPriority : memberPriorities) {
            Integer previous = priorityMap.put(memberPriority.subId(), memberPriority.priority());
            if (previous != null) {
                throw new ApplicationException(FamilyErrorCode.DUPLICATE_PRIORITY_MEMBER);
            }
        }
        return priorityMap;
    }

    private void validateMemberCoverage(List<FamilyControlMemberRow> members, Map<Long, Integer> priorityMap) {
        Set<Long> memberIds = members.stream()
                .map(FamilyControlMemberRow::subId)
                .collect(Collectors.toSet());

        if (priorityMap.size() != members.size() || !priorityMap.keySet().equals(memberIds)) {
            throw new ApplicationException(FamilyErrorCode.MISSING_PRIORITY_VALUES);
        }
    }

    private void validatePriorityValues(Map<Long, Integer> priorityMap) {
        if (priorityMap.containsValue(UNUSED_PRIORITY_ORDER)) {
            throw new ApplicationException(FamilyErrorCode.INVALID_PRIORITY_VALUE);
        }

        long uniqueCount = priorityMap.values().stream().distinct().count();
        if (uniqueCount != priorityMap.size()) {
            throw new ApplicationException(FamilyErrorCode.DUPLICATE_PRIORITY);
        }

        List<Integer> sortedPriorities = priorityMap.values().stream()
                .sorted()
                .toList();
        for (int i = 0; i < sortedPriorities.size(); i++) {
            if (sortedPriorities.get(i) != i + 1) {
                throw new ApplicationException(FamilyErrorCode.NOT_CONTINUOUS_PRIORITY);
            }
        }
    }
}
