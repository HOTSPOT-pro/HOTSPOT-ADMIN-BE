package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionUsageErrorCode implements BaseErrorCode{

    SUBSCRIPTION_LIMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION_001", "개인 데이터 전체 한도를 조회할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
