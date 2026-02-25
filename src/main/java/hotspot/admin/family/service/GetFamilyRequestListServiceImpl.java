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
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyRequestListServiceImpl implements GetFamilyRequestListService {

    private static final String FAMILY_NAME_SUFFIX = " 가족";

    private final FamilyRepository familyRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

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

        long totalElements = familyRepository.countFamilyRequestList(applyType, status);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;

        List<FamilyRequestListItem> requests = familyRepository.findFamilyRequestList(applyType, status, size, offset)
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
