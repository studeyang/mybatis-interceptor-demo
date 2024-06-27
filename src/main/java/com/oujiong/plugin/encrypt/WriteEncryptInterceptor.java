package com.oujiong.plugin.encrypt;

import com.oujiong.plugin.encrypt.EncryptField;
import com.oujiong.plugin.encrypt.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2024/6/26/026
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
@Slf4j
public class WriteEncryptInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //实体对象
        Object entity = invocation.getArgs()[1];
        Set<Field> encryptFields = ReflectionUtils.getAllFields(entity.getClass(),
                field -> field != null && field.getAnnotation(EncryptField.class) != null);

        for (Field encryptField : encryptFields) {
            // 给加密字段赋值
            encryptField.setAccessible(true);
            String plainValue = (String) encryptField.get(entity);
            String cipherValue = encrypt(plainValue);
            encryptField.set(entity, cipherValue);
        }
        return invocation.proceed();
    }

    private String encrypt(String p) throws SQLException {
        try {
            return EncryptUtils.encrypt(p);
        } catch (Exception e) {
            log.error("data encrypt Exception，parameter={}", p, e);
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
