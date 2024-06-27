package com.oujiong.plugin.encrypt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;

/**
 * 国密 SM4 加密算法(默认使用)
 */
public enum SM4Crypt implements Crypt {
    INSTANCE;

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密数据
     *
     * @param content 待加密数据
     * @return 加密后的base64字符串
     */
    @Override
    public String encrypt(String content) {
        return encrypt(content, DEFAULT_CHARSET);
    }

    /**
     * 加密数据
     *
     * @param content 待加密字符串
     * @param charset 字符集
     * @return 加密后的base64字符串
     */
    public String encrypt(String content, Charset charset) {
        return encrypt(content, ConfigUtils.getEncElement(), charset);
    }

    /**
     * 加密数据
     *
     * @param content 待加密字符串
     * @param key     密钥
     * @param charset 字符集
     * @return 加密后的base64字符串
     */
    public String encrypt(String content, String key, Charset charset) {
        // 执行加密并把加密结果的字节数组转换成Base64编码的字符串返回
        return Base64.getEncoder().encodeToString(encrypt(content.getBytes(charset), key.getBytes(charset)));
    }

    /**
     * 加密字节数组
     *
     * @param content 待加密字节数组
     * @param key     密钥字节数组
     */
    public byte[] encrypt(byte[] content, byte[] key) {
        return sm4Crypt(Cipher.ENCRYPT_MODE, content, key);
    }

    /**
     * 解密base64字符串
     *
     * @param content 加密后的base64字符串
     * @return 解密后的明文字符串（UTF-8编码）
     */
    @Override
    public String decrypt(String content) {
        return decrypt(content, DEFAULT_CHARSET);
    }

    /**
     * 解密base64字符串
     *
     * @param content 加密后的base64字符串
     * @param charset 字符集
     * @return 解密后的明文字符串（UTF-8编码）
     */
    public String decrypt(String content, Charset charset) {
        return decrypt(content, ConfigUtils.getEncElement(), charset);
    }

    /**
     * 解密base64字符串
     *
     * @param content 加密后的base64字符串
     * @param key     密钥
     * @param charset 字符集
     * @return 解密后的明文字符串（UTF-8编码）
     */
    public String decrypt(String content, String key, Charset charset) {
        byte[] buffer = decrypt(Base64.getDecoder().decode(content), key.getBytes(charset));
        return new String(buffer, charset);
    }

    /**
     * 解密字节数组
     *
     * @param content 待解密字节数组
     * @param key     密钥字节数组
     */
    public byte[] decrypt(byte[] content, byte[] key) {
        return sm4Crypt(Cipher.DECRYPT_MODE, content, key);
    }

    /**
     * AES 加密/解密
     *
     * @param content 需要 加密/解密 的内容
     * @param key     加密/解密 密钥
     */
    private byte[] sm4Crypt(int mode, byte[] content, byte[] key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");

            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS7Padding", "BC");
            cipher.init(mode, keySpec);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

}