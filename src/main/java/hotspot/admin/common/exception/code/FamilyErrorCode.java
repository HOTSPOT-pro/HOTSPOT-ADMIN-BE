package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyErrorCode implements BaseErrorCode {
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_001", "가족 정보를 찾을 수 없습니다."),
    INVALID_SIZE(HttpStatus.BAD_REQUEST, "FAMILY_002", "size는 30, 50, 100만 허용됩니다."),
    PHONE_DECRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FAMILY_003", "전화번호 복호화에 실패했습니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "FAMILY_004", "전화번호 형식이 올바르지 않습니다."),
    PHONE_HASH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FAMILY_005", "전화번호 해시 생성에 실패했습니다."),
    INVALID_APPLY_TYPE(HttpStatus.BAD_REQUEST, "FAMILY_006", "유효하지 않은 가족 요청 타입입니다."),
    INVALID_APPLY_STATUS(HttpStatus.BAD_REQUEST, "FAMILY_007", "유효하지 않은 가족 요청 상태입니다."),
    FAMILY_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_008", "가족 요청 정보를 찾을 수 없습니다."),
    FAMILY_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, "FAMILY_009", "대기중 요청만 처리할 수 있습니다."),
    FAMILY_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_010", "가족 구성원 정보를 찾을 수 없습니다."),
    INVALID_PRIORITY_VALUE(HttpStatus.BAD_REQUEST, "FAMILY_011", "우선순위 모드에서는 -1 값을 사용할 수 없습니다."),
    DUPLICATE_PRIORITY(HttpStatus.BAD_REQUEST, "FAMILY_012", "중복된 우선순위 값이 존재합니다."),
    NOT_CONTINUOUS_PRIORITY(HttpStatus.BAD_REQUEST, "FAMILY_013", "우선순위는 1부터 시작하여 연속적이어야 합니다."),
    MISSING_PRIORITY_VALUES(HttpStatus.BAD_REQUEST, "FAMILY_014", "모든 가족 구성원의 우선순위 값이 필요합니다."),
    DUPLICATE_PRIORITY_MEMBER(HttpStatus.BAD_REQUEST, "FAMILY_015", "중복된 구성원 우선순위 입력이 존재합니다."),
    INVALID_DATA_LIMIT(HttpStatus.BAD_REQUEST, "FAMILY_016", "데이터 한도는 0 이상이어야 합니다."),
    DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT(HttpStatus.BAD_REQUEST, "FAMILY_017", "데이터 한도는 가족 공유 데이터량을 초과할 수 없습니다."),
    OWNER_ROLE_NOT_UPDATABLE(HttpStatus.BAD_REQUEST, "FAMILY_018", "가족 대표(OWNER)의 부모 권한은 변경할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

}
