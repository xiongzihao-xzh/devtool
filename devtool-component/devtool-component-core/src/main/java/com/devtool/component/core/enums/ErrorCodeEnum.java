package com.devtool.component.core.enums;

import com.devtool.component.core.exception.ErrorCode;

/**
 * 错误码枚举
 *
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
public enum ErrorCodeEnum implements ErrorCode {
    SERVICE_ERROR(500, "系统执行出错");

    private final int code;
    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.message;
    }
}
