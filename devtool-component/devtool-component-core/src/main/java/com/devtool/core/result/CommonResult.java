package com.devtool.core.result;

import com.devtool.core.enums.ErrorCodeEnum;
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

    public static <T> CommonResult<T> success() {
        return result(SUCCESS_CODE, null, null);
    }

    public static <T> CommonResult<T> success(T data) {
        return result(SUCCESS_CODE, null, data);
    }

    public static <T> CommonResult<T> success(String message, T data) {
        return result(SUCCESS_CODE, message, data);
    }

    public static <T> CommonResult<T> failed() {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), ErrorCodeEnum.SERVICE_ERROR.message(), null);
    }

    public static <T> CommonResult<T> failed(String message) {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), message, null);
    }

    public static <T> CommonResult<T> failed(int code, String message) {
        return result(code, message, null);
    }

    private static <T> CommonResult<T> result(int code, String message, T data) {
        return new CommonResult<>(code, message, data);
    }
}
