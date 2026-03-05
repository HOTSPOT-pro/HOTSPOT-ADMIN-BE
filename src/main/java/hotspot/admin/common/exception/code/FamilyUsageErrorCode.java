package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyUsageErrorCode implements BaseErrorCode{

    FAMILY_LIMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_USAGE_001", "가족 공유 데이터 전체 한도를 조회할 수 없습니다"),
    FAMILY_SUB_LIMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_USAGE_002", "가족 공유 데이터 구성원별 한도를 조회할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
