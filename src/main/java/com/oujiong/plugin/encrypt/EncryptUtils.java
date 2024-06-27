package com.oujiong.plugin.encrypt;

import org.springframework.util.StringUtils;

/**
 * 加密工具类
 */
public final class EncryptUtils {

    private static final String ENCRYPTED_PREFIX = "[ENC]";

    private EncryptUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 判断字符串是否是已加密字符串
     *
     * @param content
     * @return
     */
    public static boolean isEncrypted(String content) {
        return !StringUtils.isEmpty(content) && content.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * 加密字符串
     *
     * @param content 明文 e.g. 452406199909110188
     * @return 密文
     */
    public static String encrypt(String content) {
        // 空字符串、密文字符串直接返回
        if (StringUtils.isEmpty(content) || isEncrypted(content)) {
            return content;
        }
        // 生成密文
        return ENCRYPTED_PREFIX + ConfigUtils.getCrypt().encrypt(content);
    }

    /**
     * 解密字符串
     *
     * @param content 密文 e.g. [ENC]SgNVlqy0jzzRZF15uqFSTacXVnXLf/OB/X94VrTazWM=
     * @return 原文
     */
    public static String decrypt(String content) {
        // 空字符串、非密文直接返回
        if (StringUtils.isEmpty(content) || !isEncrypted(content)) {
            return content;
        }
        //获取明文
        return ConfigUtils.getCrypt().decrypt(content.substring(ENCRYPTED_PREFIX.length()));
    }

}