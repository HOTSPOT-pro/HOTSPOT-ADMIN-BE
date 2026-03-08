package hotspot.admin.family.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.UpdateFamilyMemberControlStatusService;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import hotspot.admin.outbox.consistencyOutbox.domain.event.family.limit.FamilySubLimitChangedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.lock.SubscriptionLockedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.lock.SubscriptionUnlockedEvent;
import hotspot.admin.subscription.infrastructure.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateFamilyMemberControlStatusServiceImpl implements UpdateFamilyMemberControlStatusService {

    private static final long GB_TO_KB = 1_048_576L;

    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public void updateMemberControlStatus(
            Long familyId,
            Long subId,
            Long dataLimitGb,
            Boolean isBlocked,
            Boolean isParent
    ) {
        if (!familyRepository.existsFamilyById(familyId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }
        if (!familySubRepository.existsFamilySub(familyId, subId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
        }

        if (dataLimitGb != null) {
            long dataLimitKb = toKb(dataLimitGb);
            long familyDataAmount = familyRepository.findFamilyDataAmount(familyId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Family data amount is missing for existing familyId=" + familyId
                    ));
            if (dataLimitKb > familyDataAmount) {
                throw new ApplicationException(FamilyErrorCode.DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT);
            }

            int updated = familySubRepository.updateMemberDataLimit(familyId, subId, dataLimitKb);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }

            publishSubscriptionDataLimitEvent(familyId, subId, dataLimitKb);
        }

        if (isBlocked != null) {
            Boolean current = subscriptionJpaRepository.findIsLockedBySubId(subId);
            int updated = familySubRepository.updateMemberBlocked(familyId, subId, isBlocked);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }

            publishSubscriptionStatusEvent(subId, isBlocked, current);
        }

        if (isParent != null) {
            FamilyRole currentRole = familySubRepository.findFamilyRole(familyId, subId)
                    .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND));
            if (currentRole == FamilyRole.OWNER) {
                throw new ApplicationException(FamilyErrorCode.OWNER_ROLE_NOT_UPDATABLE);
            }

            FamilyRole targetRole = isParent ? FamilyRole.PARENT : FamilyRole.CHILD;
            int updated = familySubRepository.updateMemberFamilyRole(familyId, subId, targetRole);
            if (updated == 0) {
                throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
            }
        }
    }

    private void publishSubscriptionDataLimitEvent(Long familyId, Long subId, long dataLimitKb) {
        applicationEventPublisher.publishEvent(
                new FamilySubLimitChangedEvent(
                        "FAMILY_SUB_LIMIT_CHANGED",
                        familyId,
                        subId,
                        dataLimitKb,
                        UUID.randomUUID().toString()
                )
        );
    }

    private void publishSubscriptionStatusEvent(Long subId, Boolean isBlocked, Boolean current) {
        if (current == null) {
            return;
        }

        if (!current && isBlocked) {
            applicationEventPublisher.publishEvent(
                    new SubscriptionLockedEvent(
                            "SUBSCRIPTION_LOCKED",
                            subId,
                            UUID.randomUUID().toString()
                    )
            );
        }

        if (current && !isBlocked) {
            applicationEventPublisher.publishEvent(
                    new SubscriptionUnlockedEvent(
                            "SUBSCRIPTION_UNLOCKED",
                            subId,
                            UUID.randomUUID().toString()
                    )
            );
        }
    }

    private long toKb(Long dataLimitGb) {
        if (dataLimitGb < 0) {
            throw new ApplicationException(FamilyErrorCode.INVALID_DATA_LIMIT);
        }
        return dataLimitGb * GB_TO_KB;
    }
}
