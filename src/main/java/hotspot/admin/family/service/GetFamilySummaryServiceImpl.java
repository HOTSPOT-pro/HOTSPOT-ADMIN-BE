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
import hotspot.admin.family.service.port.FamilyQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilySummaryServiceImpl implements GetFamilySummaryService {

    private final FamilyQueryRepository familyQueryRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    /** 가족 상세 상단에 노출할 요약 정보를 조회하고 전화번호를 마스킹한다. */
    @Transactional(readOnly = true)
    @Override
    public FamilySummaryResponse getFamilySummary(Long familyId) {
        FamilyListItem family = familyQueryRepository.findFamilyById(familyId)
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

    /** 암호화된 전화번호를 복호화하고 마스킹해 화면용 항목으로 변환한다. */
    private FamilyListItem decryptAndMaskPhone(FamilyListItem item) {
        if (item.phoneNumber() == null || item.phoneNumber().isBlank()) {
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .subId(item.subId())
                    .displayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                    .representativeName(item.representativeName())
                    .phoneNumber(item.phoneNumber())
                    .memberCount(item.memberCount())
                    .build();
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(item.phoneNumber(), item.subId());
            String masked = PhoneMaskingUtil.maskMiddle(decrypted);
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .subId(item.subId())
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
