package hotspot.admin.subscription.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionRepositoryImplTest {

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    private SubscriptionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SubscriptionRepositoryImpl(subscriptionJpaRepository);
    }

    @Test
    void findIsLockedBySubId() {

        // given
        when(subscriptionJpaRepository.findIsLockedBySubId(1L))
                .thenReturn(true);

        // when
        boolean result = repository.findIsLockedBySubId(1L);

        // then
        assertThat(result).isTrue();
        verify(subscriptionJpaRepository).findIsLockedBySubId(1L);
    }
}
