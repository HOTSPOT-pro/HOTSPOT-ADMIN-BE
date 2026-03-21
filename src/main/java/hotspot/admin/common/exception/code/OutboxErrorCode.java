package hotspot.admin.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxErrorCode implements BaseErrorCode {
    OUTBOX_EVENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OUTBOX_001", "Outbox 이벤트 저장에 실패했습니다."),
    OUTBOX_PAYLOAD_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OUTBOX_002", "Outbox payload 직렬화에 실패했습니다."),
    OUTBOX_EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OUTBOX_003", "Outbox 이벤트 발행에 실패했습니다."),
    FAMILY_REQUEST_EVENT_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OUTBOX_004", "가족 요청 이벤트 생성에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
