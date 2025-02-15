package com.cyy.common.exception;

import com.cyy.common.enums.ErrorCode;

public class SystemGlobalException extends GlobalException{
    public SystemGlobalException() {
        super(ErrorCode.SYSTEM_ERROR);
    }

    public SystemGlobalException(String errorMsg) {
        super(ErrorCode.SYSTEM_ERROR, errorMsg);
    }

    public SystemGlobalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemGlobalException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }
}
