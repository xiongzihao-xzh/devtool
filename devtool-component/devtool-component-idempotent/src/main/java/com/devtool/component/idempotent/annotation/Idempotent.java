package com.devtool.component.idempotent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 * 
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Idempotent {

    /**
     * 幂等标识符
     * 
     * @return SPEL 表达式
     */
    String key() default "";

    /**
     * 
     * @return 过期时间
     */
    int expire() default 100;

    /**
     * 
     * @return 过期时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 
     * @return 提示信息
     */
    String message() default "正在执行中...";
    
}
