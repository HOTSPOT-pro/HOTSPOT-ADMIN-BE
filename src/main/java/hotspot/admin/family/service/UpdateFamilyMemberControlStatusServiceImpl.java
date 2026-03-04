package hotspot.admin.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.UpdateFamilyMemberControlStatusService;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateFamilyMemberControlStatusServiceImpl implements UpdateFamilyMemberControlStatusService {

    private static final long GB_TO_KB = 1_048_576L;

    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;

    @Transactional
    @Override
    public void updateMemberControlStatus(
            Long familyId,
            Long subId,
            Long dataLimitGb,
            Boolean isBlocked,
            Boolean isParent
    ) {
        if (!familyRepository.existsFamilyById(familyId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }
        if (!familySubRepository.existsFamilySub(familyId, subId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
        }

        if (dataLimitGb != null) {
            long dataLimitKb = toKb(dataLimitGb);
            long familyDataAmount = familyRepository.findFamilyDataAmount(familyId)
                    .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));
            if (dataLimitKb > familyDataAmount) {
                throw new ApplicationException(FamilyErrorCode.DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT);
            }

            int updated = familySubRepository.updateMemberDataLimit(familyId, subId, dataLimitKb);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }
        }

        if (isBlocked != null) {
            int updated = familySubRepository.updateMemberBlocked(familyId, subId, isBlocked);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }
        }

        if (isParent != null) {
            FamilyRole currentRole = familySubRepository.findFamilyRole(familyId, subId)
                    .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND));
            if (currentRole == FamilyRole.OWNER) {
                throw new ApplicationException(FamilyErrorCode.OWNER_ROLE_NOT_UPDATABLE);
            }

            FamilyRole targetRole = isParent ? FamilyRole.PARENT : FamilyRole.CHILD;
            int updated = familySubRepository.updateMemberFamilyRole(familyId, subId, targetRole);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }
        }
    }

    private long toKb(Long dataLimitGb) {
        if (dataLimitGb < 0) {
            throw new ApplicationException(FamilyErrorCode.INVALID_DATA_LIMIT);
        }
        return dataLimitGb * GB_TO_KB;
    }
}
