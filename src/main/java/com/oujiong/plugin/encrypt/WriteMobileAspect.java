package com.oujiong.plugin.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.reflections.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2025/3/31
 */
@Aspect
@Component
@Slf4j
public class WriteMobileAspect {

    @Pointcut("execution(* com.oujiong.mapper.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {

        Object entity = joinPoint.getArgs()[0];
        Set<Field> encryptFields = ReflectionUtils.getAllFields(entity.getClass(),
                field -> field != null && field.getAnnotation(EncryptMobileField.class) != null);

        for (Field encryptField : encryptFields) {
            // 给加密字段赋值
            encryptField.setAccessible(true);
            String plainValue = (String) encryptField.get(entity);
            String cipherValue = EncryptUtils.encrypt(plainValue);
            encryptField.set(entity, cipherValue);
        }

        return joinPoint.proceed();
    }

}
