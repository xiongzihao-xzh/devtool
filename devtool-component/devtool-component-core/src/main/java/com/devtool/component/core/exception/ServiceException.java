package com.devtool.component.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
@Getter
@Setter
@NoArgsConstructor
public class ServiceException extends RuntimeException {

    private int code;
    private String message;

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.code();
        this.message = errorCode.message();
    }

    public ServiceException(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
