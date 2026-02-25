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
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

}
