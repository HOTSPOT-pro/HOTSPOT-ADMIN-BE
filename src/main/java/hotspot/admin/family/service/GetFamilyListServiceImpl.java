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
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.service.port.FamilyQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyListServiceImpl implements GetFamilyListService {

    private final FamilyQueryRepository familyQueryRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    /** 가족 목록을 페이지 단위로 조회하고 전화번호를 마스킹해 반환한다. */
    @Transactional(readOnly = true)
    @Override
    public FamilyListResponse getFamilyList(FamilyListRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        long offset = (long) page * size;

        long totalElements = familyQueryRepository.countFamilyList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;
        List<FamilyListItem> maskedContent = familyQueryRepository.findFamilyList(size, offset).stream()
                .map(this::decryptAndMaskPhone)
                .toList();

        return FamilyListResponse.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .familyList(maskedContent)
                .build();
    }

    /** 암호화된 전화번호를 복호화/마스킹하고 화면용 항목으로 변환한다. */
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
