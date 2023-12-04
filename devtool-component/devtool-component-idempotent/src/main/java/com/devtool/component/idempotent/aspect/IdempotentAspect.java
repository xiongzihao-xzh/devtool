package com.devtool.component.idempotent.aspect;

import com.devtool.component.idempotent.annotation.Idempotent;
import com.devtool.component.idempotent.exception.IdempotentException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/22
 */
@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String SUFFIX = "idempotent";
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();

    private final RedisTemplate<String, Object> redisTemplate;

    @Pointcut("@annotation(com.devtool.component.idempotent.annotation.Idempotent)")
    public void pointCut() {}

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (!method.isAnnotationPresent(Idempotent.class)) {
            return;
        }

        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key = idempotent.key();

        if (key == null || key.isEmpty()) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (requestAttributes == null) {
                return;
            }

            HttpServletRequest request = requestAttributes.getRequest();

            StringBuffer url = request.getRequestURL();
            key = calculateSHA256(url + Arrays.toString(joinPoint.getArgs()));
        }

        BoundHashOperations<String, Object, Object> redisHashOps = redisTemplate.boundHashOps(SUFFIX);

        String message = idempotent.message();

        if (redisHashOps.get(key) != null) {
            throw new IdempotentException(message);
        }

        synchronized (this) {
            String value = LocalDateTime.now().toString().replace("T", " ");
            int expire = idempotent.expire();
            TimeUnit unit = idempotent.unit();
            Boolean absent = redisHashOps.putIfAbsent(key, value);
            redisHashOps.expire(expire, unit);
            if (absent == null || !absent) {
                throw new IdempotentException(message);
            }

            CURRENT_KEY.set(key);

            log.info("[idempotent]: key: {}, expire: {}, unit: {}", key, expire, unit);
        }
    }

    @After("pointCut()")
    public void afterPointCut() {
        String key = CURRENT_KEY.get();
        BoundHashOperations<String, Object, Object> redisHashOps = redisTemplate.boundHashOps(SUFFIX);

        if (key != null) {
            redisHashOps.delete(key);

            CURRENT_KEY.remove();
        }
    }

    private String calculateSHA256(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] messageDigest = md.digest(str.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte each : messageDigest) {
                hexString.append(String.format("%02x", each));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
