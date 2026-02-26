package hotspot.admin.family.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.outbox.dto.FamilyRequestAlertEvent;
import hotspot.admin.outbox.service.NotificationOutboxEventAppender;

@ExtendWith(MockitoExtension.class)
class FamilyRequestOutboxPublisherTest {

    @Mock
    private NotificationOutboxEventAppender outboxEventAppender;

    private FamilyRequestOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new FamilyRequestOutboxPublisher(outboxEventAppender);
    }

    @Test
    @DisplayName("승인 이벤트 outbox 적재")
    void publishApprovedSuccess() {
        FamilyApply familyApply = familyApply(11L, 101L, ApplyType.ADD);

        publisher.publishApproved(familyApply, "target-name");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventAppender).append(
                eq("user-alert"),
                eq("11"),
                eq("APPROVED"),
                payloadCaptor.capture()
        );

        FamilyRequestAlertEvent event = (FamilyRequestAlertEvent) payloadCaptor.getValue();
        assertThat(event.alertId()).isNotBlank();
        assertThat(event.eventType()).isEqualTo("APPROVED");
        assertThat(event.alertType()).isEqualTo("ADD");
        assertThat(event.targetName()).isEqualTo("target-name");
        assertThat(event.familyId()).isEqualTo(101L);
        assertThat(event.createdTime()).isNotNull();
    }

    @Test
    @DisplayName("반려 이벤트 outbox 적재")
    void publishRejectedSuccess() {
        FamilyApply familyApply = familyApply(22L, 202L, ApplyType.REMOVE);

        publisher.publishRejected(familyApply, "target-name-2");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventAppender).append(
                eq("user-alert"),
                eq("22"),
                eq("REJECTED"),
                payloadCaptor.capture()
        );

        FamilyRequestAlertEvent event = (FamilyRequestAlertEvent) payloadCaptor.getValue();
        assertThat(event.eventType()).isEqualTo("REJECTED");
        assertThat(event.alertType()).isEqualTo("REMOVE");
    }

    @Test
    @DisplayName("ApplicationException은 그대로 전파")
    void publishPropagateApplicationException() {
        FamilyApply familyApply = familyApply(1L, 1L, ApplyType.ADD);
        ApplicationException appEx = new ApplicationException(OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED);
        doThrow(appEx).when(outboxEventAppender).append(any(), any(), any(), any());

        assertThatThrownBy(() -> publisher.publishApproved(familyApply, "x"))
                .isSameAs(appEx);
    }

    @Test
    @DisplayName("기타 예외는 publish 실패 예외로 변환")
    void publishWrapUnexpectedException() {
        FamilyApply familyApply = familyApply(1L, 1L, ApplyType.ADD);
        doThrow(new RuntimeException("boom")).when(outboxEventAppender).append(any(), any(), any(), any());

        assertThatThrownBy(() -> publisher.publishApproved(familyApply, "x"))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == OutboxErrorCode.OUTBOX_EVENT_PUBLISH_FAILED);
    }

    private FamilyApply familyApply(Long familyApplyId, Long familyId, ApplyType applyType) {
        return FamilyApply.builder()
                .familyApplyId(familyApplyId)
                .requesterSubId(10L)
                .targetSubId(20L)
                .familyId(familyId)
                .applyType(applyType)
                .targetFamilyRole(FamilyRole.CHILD)
                .docUrl(null)
                .status(FamilyApplyStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();
    }
}
