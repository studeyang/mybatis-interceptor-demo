package com.oujiong.plugin;

import com.fcbox.dora.data.encrypt.log.EncryptLog;
import com.fcbox.dora.data.encrypt.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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

    /**
     * 加密字段集，From Disconf
     */
    private Map<String, String> encryptMap = new HashMap<>();

    public WriteEncryptInterceptor() {
        on();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 加密开关
        if (encryptMap.isEmpty()) {
            return invocation.proceed();
        }

        //实体对象
        Object entity = invocation.getArgs()[1];
        Set<Field> encryptFields = ReflectionUtils.getAllFields(entity.getClass(),
                field -> field != null && field.getAnnotation(EncryptField.class) != null);

        for (Field encryptField : encryptFields) {
            // 没有配置加密字段
            if (!encryptMap.containsKey(encryptField.getName())) {
                continue;
            }

            // 执行加密（双写）
            if (getSwitchFromDisconf().equals("WRITE_BOTH")) {
                // 1. 获取原字段
                String origin = encryptMap.get(encryptField.getName());
                Field originField = ReflectionUtils.getAllFields(entity.getClass(), field -> field.getName().equals(origin))
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("找不到原字段"));
                // 2. 获取原字段值
                originField.setAccessible(true);
//            originField.set(encryptField, null); // 只写原字段
                String plainValue = (String) originField.get(entity);
                // 3. 给加密字段赋值
                String cipherValue = encrypt(plainValue);
                encryptField.setAccessible(true);
                encryptField.set(entity, cipherValue);
            }
        }
        return invocation.proceed();
    }

    private String getSwitchFromDisconf() {
        return "WRITE_BOTH";
    }

    private String encrypt(String p) throws SQLException {
        try {
            String ciphertext = EncryptUtil.encrypt(p);
            // 打印日志到指定目录
            EncryptLog.newEncryptLog().withPlaintext(p).withCiphertext(ciphertext).log();
            return ciphertext;
        } catch (Exception e) {
            EncryptLog.newEncryptLog().withPlaintext(p).withExceptionInfo(e.toString()).log();
            log.error("dora data encrypt Exception，parameter={}", p, e);
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
