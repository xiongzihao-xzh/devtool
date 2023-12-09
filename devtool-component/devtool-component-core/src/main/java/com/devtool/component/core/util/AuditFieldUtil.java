package com.devtool.component.core.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/12/9
 */
@Slf4j
public class AuditFieldUtil {

    public static final String DEFAULT_CREATE_BY = "createBy";
    public static final String DEFAULT_CREATE_TIME = "createTime";
    public static final String DEFAULT_UPDATE_BY = "updateBy";
    public static final String DEFAULT_UPDATE_TIME = "updateTime";

    private AuditFieldUtil() {}

    public static <T> void fillCreateInfo(T obj, String createBy) {
        fillCreateInfo(obj, createBy, DEFAULT_CREATE_BY, DEFAULT_CREATE_TIME);
    }

    public static <T> void fillCreateInfo(
        T obj,
        String createBy,
        String createByFieldName,
        String createTimeFieldName
    ) {
        doFill(obj, createBy, createByFieldName, createTimeFieldName);
    }

    public static <T> void fillUpdateInfo(T obj, String updateBy) {
        fillUpdateInfo(obj, updateBy, DEFAULT_UPDATE_BY, DEFAULT_UPDATE_TIME);
    }

    public static <T> void fillUpdateInfo(
        T obj,
        String updateBy,
        String updateByFieldName,
        String updateTimeFieldName
    ) {
        doFill(obj, updateBy, updateByFieldName, updateTimeFieldName);
    }

    private static <T> void doFill(T obj, String operator, String operatorFieldName, String operationTimeFieldName) {
        Class<?> clazz = obj.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            String name = field.getName();

            try {
                if (operatorFieldName.equals(name)) {
                    field.setAccessible(true);
                    field.set(obj, operator);
                }

                if (operationTimeFieldName.equals(name)) {
                    field.setAccessible(true);
                    field.set(obj, new Date());
                }
            } catch (IllegalAccessException ex) {
                log.warn("数据填充失败: ", ex);
            }
        }
    }
}
