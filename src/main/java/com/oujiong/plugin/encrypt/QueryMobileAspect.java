package com.oujiong.plugin.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.reflections.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2025/3/31
 */
@Aspect
@Component
@Slf4j
public class QueryMobileAspect {

    @Pointcut("@annotation(com.oujiong.plugin.encrypt.QueryMobile)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {

        System.out.println("mapper: " + joinPoint.getSignature().getDeclaringType());

        Object arg = joinPoint.getArgs()[0];
        if (arg instanceof List) {
            List<Object> list = (List) arg;

            List<String> copyList = new ArrayList(list);
            for (String plainValue : copyList) {
                String encryptValue = EncryptUtils.encrypt(plainValue);
                list.add(encryptValue);
            }
        }

        Object dataSet = joinPoint.proceed();
        if (dataSet instanceof List) {
            return intercept((List<Object>) dataSet);
        }
        return dataSet;
    }

    private Object intercept(List<Object> dataSet) throws Throwable {

        for (Object entity : dataSet) {
            intercept(entity);
        }
        return dataSet;
    }

    private void intercept(Object entity) throws Throwable {

        Set<Field> encryptFields = ReflectionUtils.getAllFields(entity.getClass(),
                field -> field != null && field.getAnnotation(EncryptMobileField.class) != null);

        for (Field encryptField : encryptFields) {
            // 读加密字段
            encryptField.setAccessible(true);
            String cipherValue = (String) encryptField.get(entity);
            String plainValue = EncryptUtils.decrypt(cipherValue);
            encryptField.set(entity, plainValue);
        }
    }

}
