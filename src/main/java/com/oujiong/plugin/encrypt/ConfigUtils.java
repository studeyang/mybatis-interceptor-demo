package com.oujiong.plugin.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置文件工具类
 */
public final class ConfigUtils {

    private static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils() {
        throw new UnsupportedOperationException();
    }

    private static String encElement = "Aa7DdWwCc4MmX7Hh";

    private static Crypt CRYPT;

    static {
        init();
    }

    public static String getEncElement() {
        return encElement;
    }

    public static Crypt getCrypt() {
        return ConfigUtils.CRYPT;
    }

    private static void init() {
        initCrypt();
    }

    private static void initCrypt() {
        log.debug("init default crypt");
        ConfigUtils.CRYPT = SM4Crypt.INSTANCE;
        log.debug("init default crypt completed: {}", ConfigUtils.CRYPT.getClass().getName());
    }

}