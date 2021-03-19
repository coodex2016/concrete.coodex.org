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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by davidoff shen on 2017-04-24.
 */
public class RSACommon {
    /**
     * @param publicKey  publicKey
     * @param privateKey privateKey
     * @return 公私钥对是否匹配
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws SignatureException       SignatureException
     * @throws InvalidKeyException      InvalidKeyException
     */
    public static boolean isKeyPair(byte[] publicKey, byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        SecureRandom random = new SecureRandom();
        byte[] testCase = random.generateSeed(32);

        return verify(publicKey, testCase, sign(privateKey, testCase));

    }

    /**
     * @param privateKey privateKey
     * @param content    content
     * @return 私钥签名
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeyException      InvalidKeyException
     * @throws SignatureException       SignatureException
     */
    public static byte[] sign(byte[] privateKey, byte[] content) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return sign(privateKey, content, "SHA256withRSA");
    }

    /**
     * @param privateKey privateKey
     * @param content    content
     * @param algorithm  algorithm
     * @return 使用指定算法签名，默认SHA256withRSA
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeyException      InvalidKeyException
     * @throws SignatureException       SignatureException
     */
    public static byte[] sign(byte[] privateKey, byte[] content, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (privateKey == null) throw new InvalidKeyException("no privateKey.");
        RSAPrivateKey rsaPrivateKey = getRSAPrivateKey(privateKey);
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(rsaPrivateKey);
        signature.update(content);
        return signature.sign();
    }


    public static boolean verify(byte[] publicKey, byte[] content, byte[] signature) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return verify(publicKey, content, signature, "SHA256withRSA");
    }

    /**
     * @param publicKey publicKey
     * @param content   content
     * @param signature signature
     * @return 公钥验签
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeyException      InvalidKeyException
     * @throws SignatureException       SignatureException
     */
    public static boolean verify(byte[] publicKey, byte[] content, byte[] signature, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (publicKey == null) throw new InvalidKeyException("no publicKey.");
        RSAPublicKey rsaPublicKey = getRSAPublicKey(publicKey);
        Signature sign = Signature.getInstance(algorithm);
        sign.initVerify(rsaPublicKey);
        sign.update(content);
        return sign.verify(signature);
    }


    /**
     * @param publicKey publicKey
     * @param content   content
     * @return 使用RSA公钥加密
     * @throws NoSuchPaddingException    NoSuchPaddingException
     * @throws NoSuchAlgorithmException  NoSuchAlgorithmException
     * @throws InvalidKeySpecException   InvalidKeySpecException
     * @throws InvalidKeyException       InvalidKeyException
     * @throws BadPaddingException       BadPaddingException
     * @throws IllegalBlockSizeException IllegalBlockSizeException
     * @throws IOException               IOException
     */
    public static byte[] encrypt(byte[] publicKey, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        RSAPublicKey rsaKey = getRSAPublicKey(publicKey);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsaKey);

        // 待加密数据长度 <= 模长-11(PKCS1Padding算法填充位)，超过大小进行分块加密
        return rsaCrypt(content, cipher, rsaKey.getModulus().bitLength() / 8 - 11);
    }

    private static RSAPublicKey getRSAPublicKey(byte[] publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec keySpec = new X509EncodedKeySpec(publicKey);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    /**
     * @param privateKey rivateKey
     * @param content    ontent
     * @return 使用RSA私钥解密
     * @throws NoSuchPaddingException    NoSuchPaddingException
     * @throws NoSuchAlgorithmException  NoSuchAlgorithmException
     * @throws InvalidKeySpecException   InvalidKeySpecException
     * @throws InvalidKeyException       InvalidKeyException
     * @throws BadPaddingException       BadPaddingException
     * @throws IllegalBlockSizeException IllegalBlockSizeException
     * @throws IOException               IOException
     */
    public static byte[] decrypt(byte[] privateKey, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        RSAPrivateKey rsaKey = getRSAPrivateKey(privateKey);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsaKey);
        // 分块脱密
        return rsaCrypt(content, cipher, rsaKey.getModulus().bitLength() / 8);
    }

    private static RSAPrivateKey getRSAPrivateKey(byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static byte[] rsaCrypt(byte[] content, Cipher cipher, int blockSize) throws IOException, IllegalBlockSizeException, BadPaddingException {
        int remain = content.length;
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            while (remain > 0) {
                int cryptLen = Math.min(blockSize, remain);
                result.write(cipher.doFinal(content, content.length - remain, Math.min(blockSize, remain)));
                remain -= cryptLen;
            }
            return result.toByteArray();
        }
    }
}
