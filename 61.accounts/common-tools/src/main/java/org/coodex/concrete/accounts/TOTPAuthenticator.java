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

package org.coodex.concrete.accounts;

import org.apache.commons.codec.binary.Base32;
import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.DigestHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;


/**
 * TOTP (RFC6238: Time-Based One-Time Password Algorithm)的认证器实现
 * <p>
 * 参考资料：http://thegreyblog.blogspot.com/2011/12/google-authenticator-using-it-in-your.html
 * <p>
 * totp.properties<br>
 * fault.tolerance 容错范围，1-10，按照google authenticator的设计，30秒一个间隔，前后容错各fault.tolerance个间隔
 * <p>
 * app.issuer.name 核发者
 * app.server.name 服务器名
 * <p>
 * Created by davidoff shen on 2017-05-03.
 */
public class TOTPAuthenticator {

    private static final String TAG_TOTP = "totp";
//    public static final Profile_Deprecated TOTP_PROFILE = Profile_Deprecated.getProfile("totp.properties");

    public static String generateAuthKey() {
        return new Base32().encodeAsString(
                new SecureRandom(Common.getUUIDStr().getBytes())
                        .generateSeed(20));
    }

    public static boolean authenticate(String authCode, String authKey) {
        try {
            if (authCode == null || authKey == null) return false;

            return check_code(authKey, Long.valueOf(authCode),
                    Clock.currentTimeMillis()
                            / TimeUnit.SECONDS.toMillis(30));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    private static boolean check_code(String secret, long code, long t)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);

        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.

        int window = Math.max(Math.min(
                Config.getValue("fault.tolerance", 10, TAG_TOTP, getAppSet()), 10), 1);

        for (int i = -window; i <= window; ++i) {
            long hash = buildCode(decodedKey, t + i);
            if (hash == code) {
                return true;
            }
        }
        // The validation code is invalid.
        return false;
    }

    public static int buildCode(byte[] key, long t)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

//        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
//        Mac mac = Mac.getInstance("HmacSHA1");
//        mac.init(signKey);
//        byte[] hash = mac.doFinal(data);
        byte[] hash = DigestHelper.hmac(data, key, "HmacSHA1");

        int offset = hash[20 - 1] & 0xF;

        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return (int) truncatedHash;
    }

    public static String build(String authKey, String app, String userName) {
//        RecursivelyProfile profile = new RecursivelyProfile(TOTP_PROFILE);
        String issuerName = Config.getValue("issuer.name", "coodex.org", TAG_TOTP, getAppSet(), app);
        String serverName = Config.getValue("server.name", issuerName, TAG_TOTP, getAppSet(), app);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                urlEncoder(serverName), urlEncoder(userName), authKey,
                urlEncoder(issuerName));
    }

    private static String urlEncoder(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
