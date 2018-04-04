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

package org.coodex.concrete.core.signature;

import org.apache.commons.codec.binary.Base64;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.coodex.concrete.core.signature.SignUtil.PROFILE;

/**
 * base64
 * <p>
 * Created by davidoff shen on 2017-04-24.
 */
public class RSAKeyStoreDefaultImpl implements RSAKeyStore {


    /**
     * 优先级
     * rsa.privateKey.paperName.keyId
     * rsa.privateKey.paperName
     * rsa.privateKey.keyId
     * rsa.privateKey
     * <p>
     * resource:
     * paperName.keyId.pem
     * paperName.pem
     *
     * @param paperName
     * @return
     */
    @Override
    public byte[] getPrivateKey(String paperName) {
        try {
            String keyId = SignUtil.getString("keyId", paperName, null);
            return loadKey(
                    Arrays.asList("rsa.privateKey." + paperName + "." + keyId,
                            "rsa.privateKey." + paperName,
                            "rsa.privateKey." + keyId,
                            "rsa.privateKey"),
                    Arrays.asList(paperName + "." + keyId + ".pem",paperName + ".pem"));
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }

    }

    private byte[] loadKey(List<String> properties, List<String> resources) throws IOException {
        String s = null;
        for (String property : properties) {
            s = PROFILE.getString(property);
            if (s != null) break;
        }

        if (s == null) {
            InputStream is = null;
            for (String resource : resources) {
                is = RSAKeyStoreDefaultImpl.class.getClassLoader().getResourceAsStream(resource);
                if (is != null) break;
            }

            s = loadFromInputStream(is);
        }

        if (s == null) return null;

        return Base64.decodeBase64(s);
    }

    private String loadFromInputStream(InputStream is) throws IOException {
        if (is == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String s;
        while ((s = reader.readLine()) != null) {
            builder.append(s);
        }
        return builder.toString();
    }


    /**
     * 优先级
     * rsa.publicKey.paperName.keyId
     * rsa.publicKey.paperName
     * rsa.publicKey.keyId
     * rsa.publicKey
     * <p>
     * resource:
     * paperName.keyId.crt
     * paperName.crt
     *
     * @param paperName
     * @param keyId
     * @return
     */
    @Override
    public byte[] getPublicKey(String paperName, String keyId) {
        try {
            return loadKey(Arrays.asList("rsa.publicKey." + paperName + "." + keyId,
                    "rsa.publicKey." + paperName,
                    "rsa.publicKey." + keyId,
                    "rsa.publicKey"),
                    Arrays.asList(paperName + "." + keyId + ".crt", paperName + ".crt"));
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }
}
