package hotspot.admin.common.exception;

import hotspot.admin.common.exception.code.BaseErrorCode;

public class ApplicationException extends BaseException {

    public ApplicationException(BaseErrorCode code) {
        super(code);
    }

    public ApplicationException(BaseErrorCode code, Throwable cause) {
        super(code, cause);
    }
}
