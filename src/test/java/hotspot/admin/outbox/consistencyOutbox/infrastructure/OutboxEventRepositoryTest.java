package hotspot.admin.outbox.consistencyOutbox.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import hotspot.admin.outbox.consistencyOutbox.domain.OutboxEvent;
import hotspot.admin.outbox.consistencyOutbox.infrastructure.entity.OutboxEventEntity;

class OutboxEventRepositoryTest {

    private OutboxEventJpaRepository jpaRepository;
    private OutboxEventRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(OutboxEventJpaRepository.class);
        repository = new OutboxEventRepository(jpaRepository);
    }

    @Test
    @DisplayName("OutboxEvent 저장 시 Entity로 변환 후 JPA save 호출")
    void saveSuccess() {

        LocalDateTime now = LocalDateTime.now();

        OutboxEvent event = OutboxEvent.builder()
                .id("test-id")
                .aggregateType("family")
                .aggregateId("1000")
                .type("FAMILY_CREATE")
                .payload("{\"familyId\":1000}")
                .timestamp(now)
                .build();

        repository.save(event);

        ArgumentCaptor<OutboxEventEntity> captor =
                ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(jpaRepository).save(captor.capture());

        OutboxEventEntity savedEntity = captor.getValue();

        OutboxEvent result = savedEntity.entityToDomain();

        assertThat(result.getId()).isEqualTo("test-id");
        assertThat(result.getAggregateType()).isEqualTo("family");
        assertThat(result.getAggregateId()).isEqualTo("1000");
        assertThat(result.getType()).isEqualTo("FAMILY_CREATE");
        assertThat(result.getPayload()).isEqualTo("{\"familyId\":1000}");
        assertThat(result.getTimestamp()).isEqualTo(now);
    }
}
