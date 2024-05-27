package com.devtool.component.core.exception;

import com.devtool.component.core.result.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(ServiceException.class)
    public CommonResult<Void> serviceExceptionHandler(HttpServletRequest request, ServiceException ex) {
        errorLogRecord(ex.toString());

        return CommonResult.failed(ex.getCode(), ex.getMessage());
    }

    /**
     * 异常捕获兜底
     */
    @ExceptionHandler(Throwable.class)
    public CommonResult<Void> serviceExceptionHandler(HttpServletRequest request, Throwable ex) {
        errorLogRecord(ex.toString());

        return CommonResult.failed(ex.getMessage());
    }

    private void errorLogRecord(String ex) {
        log.error("[ex]: {}", ex);
    }
}
