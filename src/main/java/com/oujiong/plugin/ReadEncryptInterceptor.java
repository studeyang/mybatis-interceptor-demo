package com.oujiong.plugin;

import com.fcbox.dora.data.encrypt.log.EncryptLog;
import com.fcbox.dora.data.encrypt.util.EncryptUtil;
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

    /**
     * 加密字段集，From Disconf
     */
    private Map<String, String> encryptMap = new HashMap<>();

    public ReadEncryptInterceptor() {
        on();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object dataSet = invocation.proceed();

        // 加密开关
        if (encryptMap.isEmpty()) {
            return dataSet;
        }

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
            // 没有配置加密字段
            if (!encryptMap.containsKey(encryptField.getName())) {
                continue;
            }

            // 读加密字段
            // 1. 获取原字段
            String origin = encryptMap.get(encryptField.getName());
            Field originField = ReflectionUtils.getAllFields(entity.getClass(), field -> field.getName().equals(origin))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("找不到原字段"));
            // 2. 获取加密字段值
            encryptField.setAccessible(true);
            String cipherValue = (String) encryptField.get(entity);
            // 3. 解密并赋值
            String plainValue = decrypt(cipherValue);
            originField.setAccessible(true);
            originField.set(entity, plainValue);
        }
    }

    private String decrypt(String r) throws SQLException {
        try {
            return EncryptUtil.decrypt(r);
        } catch (Exception e) {
            EncryptLog.newEncryptLog().withCiphertext(r).withExceptionInfo(e.toString()).log();
            log.error("dora data decrypt Exception，columnValue={}", r, e);
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

    public void on() {
        encryptMap.put("nameEnc", "name");
        encryptMap.put("addressEnc", "address");
    }

    public void off() {
        encryptMap.clear();
    }

}
