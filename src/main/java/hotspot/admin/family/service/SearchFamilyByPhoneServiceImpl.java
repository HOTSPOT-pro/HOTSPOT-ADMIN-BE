package hotspot.admin.family.service;

import java.security.GeneralSecurityException;
import java.util.List;

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
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.service.port.FamilyQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchFamilyByPhoneServiceImpl implements SearchFamilyByPhoneService {

    private final FamilyQueryRepository familyQueryRepository;
    private final PhoneHashUtil phoneHashUtil;
    private final PhoneCryptoUtil phoneCryptoUtil;

    /** 전화번호 해시 검색으로 가족을 조회하고 전화번호를 마스킹해 반환한다. */
    @Transactional(readOnly = true)
    @Override
    public FamilyListResponse searchByPhone(String phoneNumber) {
        String phoneHash = createPhoneHash(phoneNumber);
        List<FamilyListItem> familyList = familyQueryRepository.findFamilyByPhoneHash(phoneHash)
                .map(this::decryptAndMaskPhone)
                .map(List::of)
                .orElse(List.of());

        return FamilyListResponse.builder()
                .page(0)
                .size(1)
                .totalElements((long) familyList.size())
                .totalPages(familyList.isEmpty() ? 0 : 1)
                .hasNext(false)
                .familyList(familyList)
                .build();
    }

    /** 입력 전화번호를 검색용 해시로 변환한다. */
    private String createPhoneHash(String phoneNumber) {
        try {
            return phoneHashUtil.hashPhone(phoneNumber);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(FamilyErrorCode.INVALID_PHONE_NUMBER);
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_HASH_FAILED);
        }
    }

    /** 조회 항목의 암호화 전화번호를 복호화/마스킹한 값으로 치환한다. */
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
