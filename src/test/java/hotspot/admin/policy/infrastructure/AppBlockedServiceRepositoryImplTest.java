package hotspot.admin.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.appservice.infrastructure.entity.AppBlockedServiceEntity;

@ExtendWith(MockitoExtension.class)
class AppBlockedServiceRepositoryImplTest {

    @Mock
    private AppBlockedServiceJpaRepository appBlockedServiceJpaRepository;

    private AppBlockedServiceRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AppBlockedServiceRepositoryImpl(appBlockedServiceJpaRepository);
    }

    @Test
    @DisplayName("앱 정책 목록 조회 성공")
    void findAllSuccess() {
        AppBlockedServiceEntity entity = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("유튜브")
                .blockedServiceCode("MEDIA_YOUTUBE")
                .isActive(true)
                .isDeleted(false)
                .build();
        Page<AppBlockedServiceEntity> page = new PageImpl<>(List.of(entity));
        when(appBlockedServiceJpaRepository.findByBlockedServiceCodeNot("PRESENT_DATA", PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<AppBlockedService> result = repository.findAll(PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAppBlockedServiceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("앱 정책 저장 성공")
    void saveSuccess() {
        AppBlockedService domain = AppBlockedService.builder()
                .blockedServiceName("유튜브")
                .blockedServiceCode("MEDIA_YOUTUBE")
                .isActive(true)
                .isDeleted(false)
                .build();
        AppBlockedServiceEntity savedEntity = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(7L)
                .blockedServiceName("유튜브")
                .blockedServiceCode("MEDIA_YOUTUBE")
                .isActive(true)
                .isDeleted(false)
                .build();
        when(appBlockedServiceJpaRepository.save(org.mockito.ArgumentMatchers.any(AppBlockedServiceEntity.class)))
                .thenReturn(savedEntity);

        AppBlockedService saved = repository.save(domain);
        assertThat(saved.getAppBlockedServiceId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("정책 코드 중복 존재 여부 조회")
    void existsByBlockedServiceCode() {
        when(appBlockedServiceJpaRepository.existsByBlockedServiceCode("MEDIA_YOUTUBE")).thenReturn(true);
        assertThat(repository.existsByBlockedServiceCode("MEDIA_YOUTUBE")).isTrue();
    }

    @Test
    @DisplayName("활성화 상태 업데이트")
    void updateActiveById() {
        when(appBlockedServiceJpaRepository.updateActiveById(1L, false)).thenReturn(1);
        assertThat(repository.updateActiveById(1L, false)).isEqualTo(1);
    }

    @Test
    @DisplayName("소프트 삭제")
    void softDeleteById() {
        when(appBlockedServiceJpaRepository.softDeleteById(1L)).thenReturn(1);
        assertThat(repository.softDeleteById(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("ID로 조회")
    void findById() {
        AppBlockedServiceEntity entity = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("유튜브")
                .blockedServiceCode("MEDIA_YOUTUBE")
                .isActive(true)
                .isDeleted(false)
                .build();
        when(appBlockedServiceJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(repository.findById(1L)).isPresent();
    }
}
