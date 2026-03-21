package hotspot.admin.outbox.consistencyOutbox.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.outbox.consistencyOutbox.domain.OutboxEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;
import hotspot.admin.outbox.consistencyOutbox.infrastructure.OutboxEventRepository;

class DomainEventOutboxListenerTest {

    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;

    private DomainEventOutboxListener listener;

    @BeforeEach
    void setUp() {
        outboxEventRepository = mock(OutboxEventRepository.class);
        objectMapper = mock(ObjectMapper.class);

        listener = new DomainEventOutboxListener(
                outboxEventRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("DomainEvent → OutboxEvent 저장 성공")
    void handleSuccess() throws Exception {

        DomainEvent event = mock(DomainEvent.class);

        when(event.aggregateType()).thenReturn("family");
        when(event.aggregateId()).thenReturn("1000");
        when(event.type()).thenReturn("FAMILY_CREATE");

        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"familyId\":1000}");

        listener.handle(event);

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();

        assertThat(saved.getAggregateType()).isEqualTo("family");
        assertThat(saved.getAggregateId()).isEqualTo("1000");
        assertThat(saved.getType()).isEqualTo("FAMILY_CREATE");
        assertThat(saved.getPayload()).isEqualTo("{\"familyId\":1000}");
    }

    @Test
    @DisplayName("JSON 직렬화 실패 시 OUTBOX_PAYLOAD_SERIALIZATION_FAILED 발생")
    void serializationFail() throws Exception {

        DomainEvent event = mock(DomainEvent.class);

        when(objectMapper.writeValueAsString(event))
                .thenThrow(new RuntimeException("json error"));

        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo(OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED);

        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Repository 저장 실패 시 OUTBOX_EVENT_SAVE_FAILED 발생")
    void saveFail() throws Exception {

        DomainEvent event = mock(DomainEvent.class);

        when(event.aggregateType()).thenReturn("family");
        when(event.aggregateId()).thenReturn("1");
        when(event.type()).thenReturn("FAMILY_CREATE");

        when(objectMapper.writeValueAsString(event))
                .thenReturn("{\"familyId\":1}");

        doThrow(new RuntimeException("db error"))
                .when(outboxEventRepository)
                .save(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo(OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED);
    }
}
