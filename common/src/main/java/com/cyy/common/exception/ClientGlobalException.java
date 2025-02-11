package com.cyy.common.exception;

import com.cyy.common.enums.ErrorCode;

public class ClientGlobalException extends GlobalException{
    public ClientGlobalException() {
        super(ErrorCode.CLIENT_ERROR);
    }
}
