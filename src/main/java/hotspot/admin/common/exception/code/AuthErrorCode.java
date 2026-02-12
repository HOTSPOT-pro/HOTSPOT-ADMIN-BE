package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인에 실패했습니다. 코드를 확인해주세요.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
