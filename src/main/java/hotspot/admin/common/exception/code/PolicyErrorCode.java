package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_001", "정책 정보를 찾을 수 없습니다."),
    INVALID_POLICY_TYPE(HttpStatus.BAD_REQUEST, "POLICY_002", "유효하지 않은 정책 타입입니다."),
    INVALID_POLICY_SNAPSHOT(HttpStatus.BAD_REQUEST, "POLICY_003", "정책 스냅샷 형식이 올바르지 않습니다."),
    DUPLICATE_POLICY_NAME_TYPE(HttpStatus.CONFLICT, "POLICY_004", "동일한 정책명과 정책유형이 이미 존재합니다."),
    DUPLICATE_POLICY_CODE(HttpStatus.CONFLICT, "POLICY_005", "동일한 정책 코드가 이미 존재합니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
