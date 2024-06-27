package com.oujiong.plugin.encrypt;

import com.oujiong.plugin.encrypt.EncryptField;
import com.oujiong.plugin.encrypt.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2024/6/26/026
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
})
@Slf4j
public class ReadEncryptInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object dataSet = invocation.proceed();

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
                field -> field != null && field.getAnnotation(EncryptField.class) != null);

        for (Field encryptField : encryptFields) {
            // 读加密字段
            encryptField.setAccessible(true);
            String cipherValue = (String) encryptField.get(entity);
            String plainValue = decrypt(cipherValue);
            encryptField.set(entity, plainValue);
        }
    }

    private String decrypt(String r) throws SQLException {
        try {
            return EncryptUtils.decrypt(r);
        } catch (Exception e) {
            log.error("data decrypt Exception，columnValue={}", r, e);
            throw new SQLException(e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
