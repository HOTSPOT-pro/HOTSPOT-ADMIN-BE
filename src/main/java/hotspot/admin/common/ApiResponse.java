package hotspot.admin.common;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import hotspot.admin.common.exception.code.BaseErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"status", "code", "message", "data"})
public class ApiResponse<T> {

    private final int status;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "200", "요청에 성공하였습니다.", data);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String code, String message, T data) {
        return new ApiResponse<>(status.value(), code, message, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(HttpStatus.OK.value(), "200", "요청에 성공하였습니다.", null);
    }

    public static <T> ApiResponse<T> fail(BaseErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getHttpStatus().value(),
                errorCode.getCustomCode(),
                errorCode.getMessage(),
                null);
    }
}
