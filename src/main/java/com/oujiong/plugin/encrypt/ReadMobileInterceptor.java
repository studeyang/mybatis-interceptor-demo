package com.oujiong.plugin.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2024/6/26/026
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
@Slf4j
public class ReadMobileInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        String mSql = sql + " limit 2";

        //通过反射修改sql语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, mSql);

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
