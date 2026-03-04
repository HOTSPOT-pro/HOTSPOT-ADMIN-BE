package hotspot.admin.outbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.outbox.notificationOutbox.infrastructure.NotificationOutboxEventJpaRepository;
import hotspot.admin.outbox.notificationOutbox.infrastructure.entity.NotificationOutboxEventEntity;
import hotspot.admin.outbox.notificationOutbox.service.NotificationOutboxEventAppender;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxEventAppenderTest {

    @Mock
    private NotificationOutboxEventJpaRepository outboxEventJpaRepository;
    @Mock
    private ObjectMapper objectMapper;

    private NotificationOutboxEventAppender appender;

    @BeforeEach
    void setUp() {
        appender = new NotificationOutboxEventAppender(outboxEventJpaRepository, objectMapper);
    }

    @Test
    @DisplayName("outbox 이벤트 저장 성공")
    void appendSuccess() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        appender.append("user-alert", "7", "APPROVED", new Object());

        ArgumentCaptor<NotificationOutboxEventEntity> captor =
                ArgumentCaptor.forClass(NotificationOutboxEventEntity.class);
        verify(outboxEventJpaRepository).save(captor.capture());

        NotificationOutboxEventEntity saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAggregateType()).isEqualTo("user-alert");
        assertThat(saved.getAggregateId()).isEqualTo("7");
        assertThat(saved.getType()).isEqualTo("APPROVED");
        assertThat(saved.getPayload()).isEqualTo("{\"ok\":true}");
    }

    @Test
    @DisplayName("payload 직렬화 실패 시 예외")
    void appendSerializationFail() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("json error") {
        });

        assertThatThrownBy(
                () -> appender.append("user-alert", "1", "APPROVED", new Object())
        )
                .isInstanceOf(ApplicationException.class)
                .matches(ex ->
                        ((ApplicationException) ex).getCode() == OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED);
    }

    @Test
    @DisplayName("DB 저장 실패 시 예외")
    void appendSaveFail() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(outboxEventJpaRepository.save(any()))
                .thenThrow(new DataAccessResourceFailureException("db error"));

        assertThatThrownBy(
                () -> appender.append("user-alert", "1", "APPROVED", new Object())
        )
                .isInstanceOf(ApplicationException.class)
                .matches(ex ->
                        ((ApplicationException) ex).getCode() == OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED);
    }
}
