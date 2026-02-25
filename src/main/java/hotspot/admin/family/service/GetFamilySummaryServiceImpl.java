package hotspot.admin.family.service;

import java.security.GeneralSecurityException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.GetFamilySummaryService;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilySummaryServiceImpl implements GetFamilySummaryService {

    private final FamilyRepository familyRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    @Transactional(readOnly = true)
    @Override
    public FamilySummaryResponse getFamilySummary(Long familyId) {
        FamilyListItem family = familyRepository.findFamilyById(familyId)
                .map(this::decryptAndMaskPhone)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        return FamilySummaryResponse.builder()
                .familyId(family.familyId())
                .displayId(family.displayId())
                .representativeName(family.representativeName())
                .phoneNumber(family.phoneNumber())
                .memberCount(family.memberCount())
                .build();
    }

    private FamilyListItem decryptAndMaskPhone(FamilyListItem item) {
        if (item.phoneNumber() == null || item.phoneNumber().isBlank()) {
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .displayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                    .representativeName(item.representativeName())
                    .phoneNumber(item.phoneNumber())
                    .memberCount(item.memberCount())
                    .build();
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(item.phoneNumber());
            String masked = PhoneMaskingUtil.maskMiddle(decrypted);
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .displayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                    .representativeName(item.representativeName())
                    .phoneNumber(masked)
                    .memberCount(item.memberCount())
                    .build();
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
