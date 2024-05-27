package com.devtool.component.core.exception;

import lombok.Getter;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
@Getter
public class ServiceException extends RuntimeException {

    private final int code;
    private final String message;

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.code();
        this.message = errorCode.message();
    }

    public ServiceException(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
