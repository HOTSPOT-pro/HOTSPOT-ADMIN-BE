package hotspot.admin.family.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.family.controller.request.PolicyActiveRequest;
import hotspot.admin.family.outbox.dto.FamilyPolicyAlertEvent;
import hotspot.admin.outbox.notificationOutbox.service.NotificationOutboxEventAppender;

@ExtendWith(MockitoExtension.class)
class FamilyPolicyOutboxPublisherTest {

    @Mock
    private NotificationOutboxEventAppender outboxEventAppender;

    private FamilyPolicyOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new FamilyPolicyOutboxPublisher(outboxEventAppender);
    }

    @Test
    @DisplayName("time policy 적용/해제 이벤트 outbox 저장")
    void publishTimeWindowPolicy() {
        List<PolicyActiveRequest> policies = List.of(
                new PolicyActiveRequest(101L, true),
                new PolicyActiveRequest(102L, false)
        );

        publisher.publishTimeWindowPolicy(1L, 10L, policies);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventAppender).append(
                eq("user-alert"),
                eq("1"),
                eq("TIME_WINDOW_POLICY"),
                payloadCaptor.capture()
        );

        FamilyPolicyAlertEvent event = (FamilyPolicyAlertEvent) payloadCaptor.getValue();
        assertThat(event.alertId()).isNotBlank();
        assertThat(event.eventType()).isEqualTo("TIME_WINDOW_POLICY");
        assertThat(event.alertType()).isEqualTo("APPLIED");
        assertThat(event.subId()).isEqualTo(10L);
        assertThat(event.familyId()).isEqualTo(1L);
        assertThat(event.createdTime()).isNotNull();
    }

    @Test
    @DisplayName("즉시차단 해제 이벤트 outbox 저장")
    void publishImmediateBlockReleased() {
        publisher.publishImmediateBlock(1L, 10L, false);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventAppender).append(
                eq("user-alert"),
                eq("1"),
                eq("IMMEDIATE_BLOCK"),
                payloadCaptor.capture()
        );

        FamilyPolicyAlertEvent event = (FamilyPolicyAlertEvent) payloadCaptor.getValue();
        assertThat(event.alertType()).isEqualTo("RELEASED");
    }

    @Test
    @DisplayName("outbox 저장 예외는 publish 예외로 변환")
    void publishWrapUnexpectedException() {
        doThrow(new RuntimeException("boom")).when(outboxEventAppender).append(any(), any(), any(), any());

        assertThatThrownBy(() -> publisher.publishServiceAccess(1L, 10L, List.of(new PolicyActiveRequest(201L, true))))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == OutboxErrorCode.OUTBOX_EVENT_PUBLISH_FAILED);
    }
}
