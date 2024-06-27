package com.oujiong.plugin.encrypt;

/**
 * 加密接口，自定义算法必须实现此接口
 *
 */
public interface Crypt {

    /**
     * 加密数据
     *
     * @param content 明文
     * @return 密文
     */
    String encrypt(String content);

    /**
     * 解密数据
     *
     * @param content 密文
     * @return 明文
     */
    String decrypt(String content);

}