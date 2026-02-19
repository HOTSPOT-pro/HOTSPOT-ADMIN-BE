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
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

}
