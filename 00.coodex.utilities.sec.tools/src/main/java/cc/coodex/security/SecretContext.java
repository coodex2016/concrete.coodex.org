package cc.coodex.security;

import java.security.PublicKey;

/**
 * 安全上下文，负责管理密钥对
 * Created by davidoff shen on 2017-02-05.
 */
public interface SecretContext {

    /**
     *
     *
     * @return 获取公钥
     */
    PublicKey getPublicKey();


    /**
     * 重置密钥对
     */
    void reset();

    /**
     *
     * @return 当前密钥对年纪
     */
    long keyAge();

    /**
     *
     *
     * @return 用私钥解密
     */
    byte[] decrypt(byte[] cipherContent);

    /**
     *
     *
     * @param content
     * @return 公钥加密
     */
    byte[] encrypt(byte[] content);

}
