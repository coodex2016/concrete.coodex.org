/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param buf       内容
     * @param algorithm 摘要算法
     * @return 信息摘要
     * @since 1.1.0[2011-9-15]
     */
    public static String digest(byte[] buf, String algorithm) {
//        MessageDigest md = null;
//        try {
//            md = MessageDigest.getInstance(algorithm);
//        } catch (NoSuchAlgorithmException e) {
//        }
//        return Common.byte2hex(md.digest(buf));
        return Common.base16Encode(digestBuff(buf, algorithm));
    }

    public static byte[] digestBuff(byte[] buf, String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm).digest(buf);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1(byte[] content) {

        return digest(content, "sha1");
    }

    public static String sha256(byte[] content) {
        return digest(content, "sha-256");
    }

    public static String md5(byte[] content) {
        return digest(content, "md5");
    }

    public static byte[] hmac(byte[] content, byte[] key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(key, "RAW");
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
        return Common.base16Encode(hmac(content.getBytes(encoding),
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


//    private static String hmacTest1(byte[] content, byte[] key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
//        SecretKey secretKey = new SecretKeySpec(key, "RAW");
//        Mac mac = Mac.getInstance(algorithm);
//        mac.init(secretKey);
//        return Common.byte2hex(mac.doFinal(content));
//    }
//
//
//    private static String hmacTest2(byte[] content, byte[] key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
//        SecretKey secretKey = new SecretKeySpec(key, algorithm);
//        Mac mac = Mac.getInstance(algorithm);
//        mac.init(secretKey);
//        return Common.byte2hex(mac.doFinal(content));
//    }
//
//    public static void main(String [] args) throws InvalidKeyException, NoSuchAlgorithmException {
////        System.out.println(sha1("1".getBytes()));
////        System.out.println(sha256("1".getBytes()));
////        System.out.println(md5("1".getBytes()));
////        System.out.println(hmacTest1("1".getBytes(), "abc".getBytes(), "HmacSHA1"));
////        System.out.println(hmacTest2("1".getBytes(), "abc".getBytes(), "HmacSHA1"));
//    }

}
