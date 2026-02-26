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
import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.service.port.FamilyApplyQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyRequestListServiceImpl implements GetFamilyRequestListService {

    private static final String FAMILY_NAME_SUFFIX = " 가족";

    private final FamilyApplyQueryRepository familyApplyQueryRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    /** 가족 요청 목록을 페이지 단위로 조회하고 화면 표시용 필드를 구성한다. */
    @Override
    @Transactional(readOnly = true)
    public FamilyRequestListResponse getFamilyRequests(
            ApplyType applyType,
            FamilyApplyStatus status,
            FamilyRequestListRequest request
    ) {
        int page = request.getPage();
        int size = request.getSize();
        long offset = (long) page * size;

        long totalElements = familyApplyQueryRepository.countFamilyRequestList(applyType, status);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;

        List<FamilyRequestListItem> requests = familyApplyQueryRepository.findFamilyRequestList(
                applyType, status, size, offset)
                .stream()
                .map(this::decryptAndMaskPhones)
                .toList();

        return FamilyRequestListResponse.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .applyType(applyType)
                .status(status)
                .requests(requests)
                .build();
    }

    /** 요청자/대상자 전화번호 마스킹 및 표시용 ID/이름을 채워 응답 항목으로 변환한다. */
    private FamilyRequestListItem decryptAndMaskPhones(FamilyRequestListItem item) {
        String requesterPhone = decryptAndMask(item.requesterPhoneNumber());
        String targetPhone = decryptAndMask(item.targetPhoneNumber());
        String familyName = item.requesterName() == null ? null : item.requesterName() + FAMILY_NAME_SUFFIX;

        return FamilyRequestListItem.builder()
                .requestId(item.requestId())
                .requestDisplayId(DisplayIdFormatter.format(DisplayIdType.FAMILY_REQUEST, item.requestId()))
                .familyId(item.familyId())
                .familyDisplayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, item.familyId()))
                .familyName(familyName)
                .requesterName(item.requesterName())
                .requesterPhoneNumber(requesterPhone)
                .targetName(item.targetName())
                .targetPhoneNumber(targetPhone)
                .targetFamilyRole(item.targetFamilyRole())
                .relationDocumentUrl(item.relationDocumentUrl())
                .requestedAt(item.requestedAt())
                .build();
    }

    /** 단일 전화번호를 복호화 후 마스킹 형식으로 변환한다. */
    private String decryptAndMask(String phoneEnc) {
        if (phoneEnc == null || phoneEnc.isBlank()) {
            return phoneEnc;
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(phoneEnc);
            return PhoneMaskingUtil.maskMiddle(decrypted);
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
