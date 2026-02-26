package hotspot.admin.outbox.service;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.outbox.infrastructure.NotificationOutboxEventJpaRepository;
import hotspot.admin.outbox.infrastructure.entity.NotificationOutboxEventEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationOutboxEventAppender {

    private final NotificationOutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    // 단일 outbox 이벤트 레코드를 outbox_event 테이블에 저장한다.
    public void append(String aggregateType, String aggregateId, String type, Object payload) {
        try {
            outboxEventJpaRepository.save(
                    NotificationOutboxEventEntity.builder()
                            .id(UUID.randomUUID())
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .type(type)
                            .payload(toJson(payload))
                            .build()
            );
        } catch (DataAccessException ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED, ex);
        }
    }

    // payload 객체를 JSON 문자열로 직렬화한다.
    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED, ex);
        }
    }
}
