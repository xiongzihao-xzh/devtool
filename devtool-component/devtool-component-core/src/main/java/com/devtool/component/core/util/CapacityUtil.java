package com.devtool.component.core.util;

/**
 * CapacityUtil
 *
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2024-05-03
 */
public class CapacityUtil {

    private CapacityUtil() {}

    public static int getCapacity(int size) {
        return Math.max((int) (size / 0.75f) + 1, 16);
    }
}
