package hotspot.admin.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;

public interface FamilyApplyJpaRepository extends JpaRepository<FamilyApplyEntity, Long> {
    /** 요청 ID/유형/현재 상태가 일치할 때만 상태를 갱신한다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE family_apply
                    SET status = :newStatus,
                        modified_time = now()
                    WHERE family_apply_id = :familyApplyId
                      AND apply_type = :applyType
                      AND status = :currentStatus
                    """,
            nativeQuery = true
    )
    int updateStatusByIdAndTypeAndCurrentStatus(
            @Param("familyApplyId") Long familyApplyId,
            @Param("applyType") ApplyType applyType,
            @Param("currentStatus") FamilyApplyStatus currentStatus,
            @Param("newStatus") FamilyApplyStatus newStatus
    );

    /** 요청 ID와 요청 유형으로 요청 존재 여부를 확인한다. */
    boolean existsByFamilyApplyIdAndApplyType(Long familyApplyId, ApplyType applyType);
}
