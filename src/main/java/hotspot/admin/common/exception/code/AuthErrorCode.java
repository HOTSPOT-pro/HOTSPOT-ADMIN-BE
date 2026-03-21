package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인에 실패했습니다. 코드를 확인해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원되지 않는 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "토큰이 비어있거나 잘못되었습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
