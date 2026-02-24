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

import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.infrastructure.entity.BlockPolicyEntity;

@ExtendWith(MockitoExtension.class)
class BlockPolicyRepositoryImplTest {

    @Mock
    private BlockPolicyJpaRepository blockPolicyJpaRepository;

    private BlockPolicyRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new BlockPolicyRepositoryImpl(blockPolicyJpaRepository);
    }

    @Test
    @DisplayName("시간 정책 목록 조회 성공")
    void findAllSuccess() {
        BlockPolicyEntity entity = BlockPolicyEntity.builder()
                .blockPolicyId(1L)
                .policyName("수면모드")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(PolicySnapshot.builder().startTime("00:00").endTime("07:00").build())
                .isActive(true)
                .isDeleted(false)
                .build();

        Page<BlockPolicyEntity> page = new PageImpl<>(List.of(entity));
        when(blockPolicyJpaRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        Page<BlockPolicy> result = repository.findAll(PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBlockPolicyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("시간 정책 저장 성공")
    void saveSuccess() {
        BlockPolicy domain = BlockPolicy.builder()
                .policyName("수면모드")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(PolicySnapshot.builder().startTime("00:00").endTime("07:00").build())
                .isActive(true)
                .isDeleted(false)
                .build();

        BlockPolicyEntity savedEntity = BlockPolicyEntity.builder()
                .blockPolicyId(10L)
                .policyName("수면모드")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(domain.getPolicySnapshot())
                .isActive(true)
                .isDeleted(false)
                .build();

        when(blockPolicyJpaRepository.save(org.mockito.ArgumentMatchers.any(BlockPolicyEntity.class)))
                .thenReturn(savedEntity);

        BlockPolicy saved = repository.save(domain);
        assertThat(saved.getBlockPolicyId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("정책명+유형 중복 존재 여부 조회")
    void existsByPolicyNameAndPolicyType() {
        when(blockPolicyJpaRepository.existsByPolicyNameAndPolicyType("수면모드", "SCHEDULED")).thenReturn(true);
        assertThat(repository.existsByPolicyNameAndPolicyType("수면모드", "SCHEDULED")).isTrue();
    }

    @Test
    @DisplayName("활성화 상태 업데이트")
    void updateActiveById() {
        when(blockPolicyJpaRepository.updateActiveById(1L, true)).thenReturn(1);
        assertThat(repository.updateActiveById(1L, true)).isEqualTo(1);
    }

    @Test
    @DisplayName("소프트 삭제")
    void softDeleteById() {
        when(blockPolicyJpaRepository.softDeleteById(1L)).thenReturn(1);
        assertThat(repository.softDeleteById(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("ID로 조회")
    void findById() {
        BlockPolicyEntity entity = BlockPolicyEntity.builder()
                .blockPolicyId(1L)
                .policyName("수면모드")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(PolicySnapshot.builder().startTime("00:00").endTime("07:00").build())
                .isActive(true)
                .isDeleted(false)
                .build();
        when(blockPolicyJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(repository.findById(1L)).isPresent();
    }
}
