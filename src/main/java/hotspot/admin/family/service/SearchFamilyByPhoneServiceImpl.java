package hotspot.admin.family.service;

import java.security.GeneralSecurityException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneHashUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchFamilyByPhoneServiceImpl implements SearchFamilyByPhoneService {

    private final FamilyRepository familyRepository;
    private final PhoneHashUtil phoneHashUtil;
    private final PhoneCryptoUtil phoneCryptoUtil;

    @Transactional(readOnly = true)
    @Override
    public FamilyPhoneSearchResponse searchByPhone(String phoneNumber) {
        String phoneHash = createPhoneHash(phoneNumber);
        FamilyListItem family = familyRepository.findFamilyByPhoneHash(phoneHash)
                .map(this::decryptAndMaskPhone)
                .orElse(null);

        return FamilyPhoneSearchResponse.builder()
                .family(family)
                .build();
    }

    private String createPhoneHash(String phoneNumber) {
        try {
            return phoneHashUtil.hashPhone(phoneNumber);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(FamilyErrorCode.INVALID_PHONE_NUMBER);
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_HASH_FAILED);
        }
    }

    private FamilyListItem decryptAndMaskPhone(FamilyListItem item) {
        if (item.phoneNumber() == null || item.phoneNumber().isBlank()) {
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .displayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                    .representativeName(item.representativeName())
                    .phoneNumber(item.phoneNumber())
                    .memberCount(item.memberCount())
                    .usedData(item.usedData())
                    .remainingData(item.remainingData())
                    .build();
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(item.phoneNumber());
            String masked = PhoneMaskingUtil.maskMiddle(decrypted);
            // [TODO] usedData/remainingData는 사용량 집계 연동 전까지 null 유지.
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .displayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                    .representativeName(item.representativeName())
                    .phoneNumber(masked)
                    .memberCount(item.memberCount())
                    .usedData(item.usedData())
                    .remainingData(item.remainingData())
                    .build();
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
