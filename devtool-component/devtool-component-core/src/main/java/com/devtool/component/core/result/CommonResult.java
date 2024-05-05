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
public class CommonResult {

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
    private Object data;

    public static CommonResult success() {
        return result(SUCCESS_CODE, null, null);
    }

    public static CommonResult success(Object data) {
        return result(SUCCESS_CODE, null, data);
    }

    public static CommonResult success(String message, Object data) {
        return result(SUCCESS_CODE, message, data);
    }

    public static CommonResult failed() {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), ErrorCodeEnum.SERVICE_ERROR.message(), null);
    }

    public static CommonResult failed(String message) {
        return result(ErrorCodeEnum.SERVICE_ERROR.code(), message, null);
    }

    public static CommonResult failed(ErrorCode errorCode) {
        return result(errorCode.code(), errorCode.message(), null);
    }

    public static CommonResult failed(int code, String message) {
        return result(code, message, null);
    }

    private static CommonResult result(int code, String message, Object data) {
        return new CommonResult(code, message, data);
    }
}
