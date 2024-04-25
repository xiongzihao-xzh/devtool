package com.devtool.mybatis.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class FillOptAuditFieldInterceptor implements Interceptor {

    private static final String INSERT_TYPE = SqlCommandType.INSERT.name();
    private static final String UPDATE_TYPE = SqlCommandType.UPDATE.name();
    private static final String CREATE_BY = "createBy";
    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_BY = "updateBy";
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            String sqlCommandType = ms.getSqlCommandType().name();
            Object paramObj = args[1];

            if (INSERT_TYPE.equals(sqlCommandType) || UPDATE_TYPE.equals(sqlCommandType)) {
                if (Map.class.isAssignableFrom(paramObj.getClass())) {
                    MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) paramObj;
                    for (Object actualParamObj : paramMap.values()) {
                        if(actualParamObj instanceof List<?>){
                            List<?> ListParamObj = (List<?>) actualParamObj;
                            for (Object each : ListParamObj) {
                                fillField(each, sqlCommandType);
                            }
                        }
                        
                        fillField(actualParamObj, sqlCommandType);
                    }
                } else {
                    fillField(paramObj, sqlCommandType);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("mybatis intercept FillCustomInterceptor 自动填充字段出错: ", e);
        }

        return invocation.proceed();
    }

    private void fillField(Object paramObj, String sqlCommandType) throws IllegalAccessException {
        Class<?> paramObjClass = paramObj.getClass();
        Field[] paramObjFields = paramObjClass.getDeclaredFields();

        for (Field field : paramObjFields) {
            String fileName = field.getName();
            if (INSERT_TYPE.equals(sqlCommandType) && CREATE_BY.equals(fileName)) {
                setOperatorField(paramObj, field);
            }
            if (INSERT_TYPE.equals(sqlCommandType) && CREATE_TIME.equals(fileName)) {
                setDateTimeField(paramObj, field);
            }
            if (UPDATE_TYPE.equals(sqlCommandType) && UPDATE_BY.equals(fileName)) {
                setOperatorField(paramObj, field);
            }
            if (UPDATE_TYPE.equals(sqlCommandType) && UPDATE_TIME.equals(fileName)) {
                setDateTimeField(paramObj, field);
            }
        }
    }

    private void setDateTimeField(Object actualParamObj, Field field) throws IllegalAccessException {
        if (Date.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            field.set(actualParamObj, new Date());
        } else if (LocalDateTime.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            field.set(actualParamObj, LocalDateTime.now());
        }
    }

    private void setOperatorField(Object actualParamObj, Field field) throws IllegalAccessException {
        if (String.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            field.set(actualParamObj, "xzh");
        }
    }
}