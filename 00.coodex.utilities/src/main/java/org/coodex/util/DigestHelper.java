/**
 *
 */
package org.coodex.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author davidoff
 */
public class DigestHelper {

    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 获取信息摘要
     *
     * @param buf 内容
     * @param al  摘要类型
     * @return 信息摘要
     * @since 1.1.0[2011-9-15]
     */
    private static String digest(byte[] buf, String al) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(al);
        } catch (NoSuchAlgorithmException e) {
        }
        return Common.byte2hex(md.digest(buf));
    }

    public static String sha1(byte[] content) {

        return digest(content, "sha1");
    }

    public static byte[] hmac(byte[] content, byte[] key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(key, algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKey);
        return mac.doFinal(content);
    }

    public static byte[] hmac(byte[] content, byte[] key)
            throws NoSuchAlgorithmException,
            InvalidKeyException {
        return hmac(content, key, "HmacSHA1");
//      SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");
//      Mac mac = Mac.getInstance("HmacSHA1");
//      mac.init(secretKey);
//      return mac.doFinal(content);
    }

    public static String hmac(String content, String key, String algorithm, String encoding)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        if (content == null)
            content = "";
        if (key == null)
            throw new NullPointerException("hmac key is NULL.");
        return Common.byte2hex(hmac(content.getBytes(encoding),
                key.getBytes(encoding), algorithm));
    }

    public static String hmac(String content, String key, String algorithm)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        return hmac(content, key, algorithm, DEFAULT_ENCODING);
//        if (content == null)
//            content = "";
//        if (key == null)
//            throw new NullPointerException("hmac key is NULL.");
//        return Common.byte2hex(hmac(content.getBytes(DEFAULT_ENCODING),
//                key.getBytes(DEFAULT_ENCODING), algorithm));
    }

    public static String hmac(String content, String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        return hmac(content, key, "HmacSHA1");
//        if (content == null)
//            content = "";
//        if (key == null)
//            throw new NullPointerException("hmac key is NULL.");
//        return Common.byte2hex(hmac(content.getBytes(DEFAULT_ENCODING),
//                key.getBytes(DEFAULT_ENCODING)));

    }

}
