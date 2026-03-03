package hotspot.admin.family.service;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import hotspot.admin.family.controller.response.FamilyRequestTargetItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.query.dto.FamilyRequestListRow;
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

        List<FamilyRequestListItem> requests = toRequestItems(
                familyApplyQueryRepository.findFamilyRequestList(applyType, status, size, offset)
        );

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

    /** 요청 행 목록을 요청 단위로 그룹핑해 대상자 다건 응답 형태로 변환한다. */
    private List<FamilyRequestListItem> toRequestItems(List<FamilyRequestListRow> rows) {
        Map<Long, RequestAccumulator> grouped = new LinkedHashMap<>();

        for (FamilyRequestListRow row : rows) {
            RequestAccumulator acc = grouped.computeIfAbsent(row.requestId(), key -> new RequestAccumulator(
                    row.requestId(),
                    row.familyId(),
                    row.requestSubId(),
                    row.requesterName(),
                    decryptAndMask(row.requesterPhoneNumberEnc()),
                    row.relationDocumentUrl(),
                    row.requestedAt()
            ));

            if (row.targetName() != null || row.targetPhoneNumberEnc() != null || row.targetFamilyRole() != null) {
                acc.targets.add(FamilyRequestTargetItem.builder()
                        .targetSubId(row.targetSubId())
                        .targetName(row.targetName())
                        .targetPhoneNumber(decryptAndMask(row.targetPhoneNumberEnc()))
                        .targetFamilyRole(row.targetFamilyRole())
                        .build());
            }
        }

        return grouped.values().stream().map(this::toRequestItem).toList();
    }

    private FamilyRequestListItem toRequestItem(RequestAccumulator acc) {
        String familyName = acc.requesterName == null ? null : acc.requesterName + FAMILY_NAME_SUFFIX;
        return FamilyRequestListItem.builder()
                .requestId(acc.requestId)
                .requestDisplayId(DisplayIdFormatter.format(DisplayIdType.FAMILY_REQUEST, acc.requestId))
                .familyId(acc.familyId)
                .familyDisplayId(DisplayIdFormatter.format(DisplayIdType.FAMILY, acc.familyId))
                .familyName(familyName)
                .requestSubId(acc.requestSubId)
                .requesterName(acc.requesterName)
                .requesterPhoneNumber(acc.requesterPhoneNumber)
                .targets(acc.targets)
                .relationDocumentUrl(acc.relationDocumentUrl)
                .requestedAt(acc.requestedAt)
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

    private static final class RequestAccumulator {
        private final Long requestId;
        private final Long familyId;
        private final Long requestSubId;
        private final String requesterName;
        private final String requesterPhoneNumber;
        private final String relationDocumentUrl;
        private final java.time.LocalDateTime requestedAt;
        private final List<FamilyRequestTargetItem> targets = new ArrayList<>();

        private RequestAccumulator(
                Long requestId,
                Long familyId,
                Long requestSubId,
                String requesterName,
                String requesterPhoneNumber,
                String relationDocumentUrl,
                java.time.LocalDateTime requestedAt
        ) {
            this.requestId = requestId;
            this.familyId = familyId;
            this.requestSubId = requestSubId;
            this.requesterName = requesterName;
            this.requesterPhoneNumber = requesterPhoneNumber;
            this.relationDocumentUrl = relationDocumentUrl;
            this.requestedAt = requestedAt;
        }
    }
}
