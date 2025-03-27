package com.cyy.common.exception;

import com.cyy.common.enums.ErrorCode;

public class ApplicationNoModelConfigException extends SystemGlobalException{
    public ApplicationNoModelConfigException() {
        super(ErrorCode.APPLICATION_NO_MODEL_CONFIG_ERROR);
    }
}
