package hotspot.admin.family.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyListServiceImpl implements GetFamilyListService {

    private static final Set<Integer> ALLOWED_SIZE = Set.of(30, 50, 100);

    private final FamilyRepository familyRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    @Override
    public FamilyListResponse getFamilyList(FamilyListRequest request) {
        validateSize(request.getSize());

        long cursor = request.getCursor() == null ? 0L : request.getCursor();
        List<FamilyListItem> rows = familyRepository.findFamilySlice(request.getSize() + 1, cursor);

        boolean hasNext = rows.size() > request.getSize();
        List<FamilyListItem> content = hasNext ? rows.subList(0, request.getSize()) : rows;
        List<FamilyListItem> maskedContent = content.stream()
                .map(this::decryptAndMaskPhone)
                .toList();
        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).familyId()
                : null;

        return FamilyListResponse.builder()
                .size(request.getSize())
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .familyList(maskedContent)
                .build();
    }

    private void validateSize(Integer size) {
        if (size == null || !ALLOWED_SIZE.contains(size)) {
            throw new ApplicationException(FamilyErrorCode.INVALID_SIZE);
        }
    }

    private FamilyListItem decryptAndMaskPhone(FamilyListItem item) {
        if (item.phoneNumber() == null || item.phoneNumber().isBlank()) {
            return item;
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(item.phoneNumber());
            String masked = PhoneMaskingUtil.maskMiddle(decrypted);
            return FamilyListItem.builder()
                    .familyId(item.familyId())
                    .representativeName(item.representativeName())
                    .phoneNumber(masked)
                    .memberCount(item.memberCount())
                    .usedData(item.usedData())
                    .remainingData(item.remainingData())
                    .build();
        } catch (Exception e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
