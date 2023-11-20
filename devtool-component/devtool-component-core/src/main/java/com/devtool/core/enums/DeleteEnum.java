package com.devtool.core.enums;

/**
 * 逻辑删除标记
 * 
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/20
 */
public enum DeleteEnum {
    IN_USE(0),
    DELETE(1);

    private final int code;

    DeleteEnum(int code) {
        this.code = code;
    }

    private int code() {
        return this.code;
    }
}
