package hotspot.admin.outbox.consistencyOutbox.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.outbox.consistencyOutbox.domain.OutboxEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;
import hotspot.admin.outbox.consistencyOutbox.infrastructure.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DomainEventOutboxListener {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(DomainEvent event) {

        try {
            String payLoad = toJson(event);

            OutboxEvent outbox = OutboxEvent.create(
                    event.aggregateType(),
                    event.aggregateId(),
                    event.type(),
                    payLoad
            );

            outboxEventRepository.save(outbox);

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED, e);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED, ex);
        }
    }
}
