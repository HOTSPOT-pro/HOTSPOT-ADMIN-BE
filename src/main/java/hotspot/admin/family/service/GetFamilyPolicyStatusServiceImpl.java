package hotspot.admin.family.service;

import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.GetFamilyPolicyStatusService;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyPolicyStatusServiceImpl implements GetFamilyPolicyStatusService {

    private final FamilyRepository familyRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;

    /** 구성원별 적용 정책(시간대/차단 서비스)과 차단 상태를 묶어 반환한다. */
    @Transactional(readOnly = true)
    @Override
    public List<FamilyPolicyMemberStatusItem> getFamilyPolicyStatus(Long familyId) {
        familyRepository.findFamilyById(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        return familyRepository.findFamilyPolicyStatusRows(familyId).stream()
                .map(this::toMemberItem)
                .toList();
    }

    /** 통합 조회 행을 정책 탭 응답 항목으로 변환한다. */
    private FamilyPolicyMemberStatusItem toMemberItem(FamilyPolicyStatusRow member) {
        return FamilyPolicyMemberStatusItem.builder()
                .memberName(member.memberName())
                .phoneNumber(decryptAndMaskPhone(member.phoneNumberEnc()))
                .familyRole(member.familyRole())
                .blocked(Boolean.TRUE.equals(member.blocked()))
                .appliedTimePolicies(member.appliedTimePolicies())
                .appliedBlockedServicePolicies(member.appliedBlockedServicePolicies())
                .build();
    }

    /** 암호화된 전화번호를 복호화하고 마스킹해 표시 형식으로 변환한다. */
    private String decryptAndMaskPhone(String encryptedPhone) {
        if (encryptedPhone == null || encryptedPhone.isBlank()) {
            return encryptedPhone;
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(encryptedPhone);
            return PhoneMaskingUtil.maskMiddle(decrypted);
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
