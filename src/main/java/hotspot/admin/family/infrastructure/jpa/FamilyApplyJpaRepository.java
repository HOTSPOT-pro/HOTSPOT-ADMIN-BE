package hotspot.admin.family.infrastructure.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;

public interface FamilyApplyJpaRepository extends JpaRepository<FamilyApplyEntity, Long> {
    /** 대기중 가족 요청만 목표 상태(승인/반려)로 조건부 업데이트한다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilyApplyEntity fa
            SET fa.status = :newStatus
            WHERE fa.familyApplyId = :familyApplyId
              AND fa.applyType = :applyType
              AND fa.status = :currentStatus
            """)
    int updateStatusByIdAndTypeAndCurrentStatus(
            @Param("familyApplyId") Long familyApplyId,
            @Param("applyType") ApplyType applyType,
            @Param("currentStatus") FamilyApplyStatus currentStatus,
            @Param("newStatus") FamilyApplyStatus newStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FamilyApplyEntity fa
            SET fa.family = :family
            WHERE fa.familyApplyId = :familyApplyId
              AND fa.applyType = :applyType
            """)
    int updateFamilyByIdAndType(
            @Param("familyApplyId") Long familyApplyId,
            @Param("applyType") ApplyType applyType,
            @Param("family") FamilyEntity family
    );

    /** 요청 ID와 요청 유형으로 요청 존재 여부를 확인한다. */
    boolean existsByFamilyApplyIdAndApplyType(Long familyApplyId, ApplyType applyType);

    /** 요청 ID와 요청 유형으로 신청자 subId를 조회한다. */
    @Query("""
            SELECT rs.subId
            FROM FamilyApplyEntity fa
            JOIN fa.requesterSubscription rs
            WHERE fa.familyApplyId = :familyApplyId
              AND fa.applyType = :applyType
            """)
    Optional<Long> findRequesterSubIdByFamilyApplyIdAndApplyType(
            @Param("familyApplyId") Long familyApplyId,
            @Param("applyType") ApplyType applyType
    );

    /** 요청 ID와 요청 유형으로 Outbox 생성에 필요한 정보를 단건 조회한다. */
    @Query("""
            SELECT fa
            FROM FamilyApplyEntity fa
            JOIN FETCH fa.requesterSubscription rs
            LEFT JOIN FETCH fa.family f
            LEFT JOIN FETCH fa.targets fat
            LEFT JOIN FETCH fat.targetSubscription ts
            LEFT JOIN FETCH ts.member tm
            WHERE fa.familyApplyId = :familyApplyId
              AND fa.applyType = :applyType
            """)
    Optional<FamilyApplyEntity> findOutboxSourceByFamilyApplyIdAndApplyType(
            @Param("familyApplyId") Long familyApplyId,
            @Param("applyType") ApplyType applyType
    );

    /** 요청 대상자의 이름 목록을 조회한다. */
    @Query(
            value = """
                    SELECT m.name
                    FROM family_apply_target fat
                    JOIN subscription s ON s.sub_id = fat.target_sub_id
                    JOIN member m ON m.member_id = s.member_id
                    WHERE fat.family_apply_id = :familyApplyId
                    ORDER BY fat.family_apply_target_id
                    """,
            nativeQuery = true
    )
    List<String> findTargetNamesByFamilyApplyId(@Param("familyApplyId") Long familyApplyId);

    /** 요청 ID와 요청 유형으로 요청 엔티티를 조회한다. */
    Optional<FamilyApplyEntity> findByFamilyApplyIdAndApplyType(Long familyApplyId, ApplyType applyType);
}
