package hotspot.admin.usage.subscriptionUsage.service;

import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.domain.FamilySub;
import hotspot.admin.family.service.port.FamilySubRepository;
import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.subscription.domain.Subscription;
import hotspot.admin.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.admin.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.admin.usage.subscriptionUsage.domain.mapper.SubscriptionUsageMapper;
import hotspot.admin.usage.subscriptionUsage.infrastructure.repository.PresentDataJdbcRepository;
import hotspot.admin.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSubscriptionUsageServiceImpl implements FindSubscriptionUsageService {

    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final PresentDataJdbcRepository presentDataJdbcRepository;
    private final FamilySubRepository familySubRepository;
    private final Clock clock;
    private final PhoneCryptoUtil phoneCryptoUtil;


    @Transactional(readOnly = true)
    @Override
    public List<SubscriptionUsageResponse> findSubscriptionUsage(Long familyId) {

        List<FamilySub> familySubs =
                familySubRepository.findByFamilyId(familyId);

        Map<Long, DataPeriod> subPeriodMap =
                familySubs.stream()
                        .collect(Collectors.toMap(
                                fs -> fs.getSubscription().getSubId(),
                                fs -> fs.getSubscription()
                                        .getPlan()
                                        .getDataPeriod()
                        ));

        Map<Long, SubscriptionUsage> usageMap =
                subscriptionUsageRepository
                        .findSubscriptionUsages(subPeriodMap);

        List<Long> giftIds =
                usageMap.values()
                        .stream()
                        .flatMap(u -> u.gifts().stream())
                        .map(GiftUsage::giftId)
                        .distinct()
                        .toList();

        Map<Long, String> giftIdToUserName =
                presentDataJdbcRepository.findGiftGiverNames(giftIds);

        LocalDateTime now = LocalDateTime.now(clock);

        return familySubs.stream()
                .map(familySub -> {

                    Subscription subscription = familySub.getSubscription();
                    Long subId = subscription.getSubId();

                    SubscriptionUsage usage =
                            usageMap.get(subId);

                    String maskedPhone = decryptAndMaskPhone(subscription.getPhoneEnc());

                    return SubscriptionUsageMapper
                            .toSubscriptionUsageResponse(
                                    usage,
                                    maskedPhone,
                                    familySub.getFamilyRole(),
                                    subscription,
                                    giftIdToUserName,
                                    now
                            );
                })
                .toList();
    }

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
