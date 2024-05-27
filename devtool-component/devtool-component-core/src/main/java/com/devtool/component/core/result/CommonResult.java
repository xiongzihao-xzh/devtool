package com.devtool.component.core.result;

import com.devtool.component.core.enums.ErrorCodeEnum;
import com.devtool.component.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回对象
 *
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {

    public static final int SUCCESS_CODE = 200;

    /**
     * 响应码
     */
    private int code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    public static <R> CommonResult<R> success() {
        return result(SUCCESS_CODE, null, null);
    }

    public static <R> CommonResult<R> success(R data) {
        return result(SUCCESS_CODE, null, data);
    }

    public static <R> CommonResult<R> success(String message, R data) {
        return result(SUCCESS_CODE, message, data);
    }

    public static <R> CommonResult<R> failed() {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), ErrorCodeEnum.SERVICE_ERROR.message(), null);
    }

    public static <R> CommonResult<R> failed(String message) {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), message, null);
    }

    public static <R> CommonResult<R> failed(ErrorCode errorCode) {
        return result(errorCode.code(), errorCode.message(), null);
    }

    public static <R> CommonResult<R> failed(int code, String message) {
        return result(code, message, null);
    }

    private static <R> CommonResult<R> result(int code, String message, R data) {
        return new CommonResult<>(code, message, data);
    }
}
