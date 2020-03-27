/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.coodex.concrete.common.ErrorCodes.SIGNATURE_KEY_LOAD_FAILED;
import static org.coodex.concrete.core.signature.SignUtil.getString;

public class RSA_KeyStoreDefaultImpl implements RSA_KeyStore {
//    private final static Logger log = LoggerFactory.getLogger(RSA_KeyStoreDefaultImpl.class);

//    // TO DO 0.4.2 移除
//    @SuppressWarnings("deprecation")
//    private static final ServiceLoaderImpl<RSAKeyStore> COMPATIBILITY_LOADER = new ServiceLoaderImpl<RSAKeyStore>(
//            new RSAKeyStoreDefaultImpl()
//    ) {
//    };

    static byte[] loadKey(List<String> properties, List<String> resources) throws IOException {
        String s = null;
        for (String property : properties) {
            s = getString(property, null, null);
            if (s != null) break;
        }

        if (s == null) {
            URL url = null;
            for (String resource : resources) {
                url = Common.getResource("rsaKeys/" + resource);
                if (url != null) break;
            }
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    s = loadFromInputStream(is);
                }
            }
        }

        return s == null ? null : Base64.decodeBase64(s);
    }

    private static String loadFromInputStream(InputStream is) throws IOException {
        if (is == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String s;
        while ((s = reader.readLine()) != null) {
            builder.append(s);
        }
        return builder.toString();
    }

    static List<String> getConfigKeys(String paperName, String keyId, String type) {
        List<String> list = new ArrayList<>();
        boolean paperNameIsBlank = Common.isBlank(paperName), keyIdIsBlank = Common.isBlank(keyId);
        if (!paperNameIsBlank) {
            if (!keyIdIsBlank) {
                list.add("rsa." + type + "." + paperName + "." + keyId);
            }
            list.add("rsa." + type + "." + paperName);
        }
        if (!keyIdIsBlank) {
            list.add("rsa." + type + "." + keyId);
        }
        list.add("rsa." + type);
        return list;
    }

    static List<String> getResourceList(String paperName, String keyId, String type) {
        List<String> list = new ArrayList<>();
        boolean paperNameIsBlank = Common.isBlank(paperName), keyIdIsBlank = Common.isBlank(keyId);
        if (!keyIdIsBlank) {
            if (!paperNameIsBlank) {
                list.add(paperName + "." + keyId + "." + type);
            }
            list.add(keyId + "." + type);
        }
        if (!paperNameIsBlank) {
            list.add(paperName + "." + type);
        }
        return list;
    }

    /**
     * 优先级
     * rsa.privateKey.paperName.keyId
     * rsa.privateKey.paperName
     * rsa.privateKey.keyId
     * rsa.privateKey
     * <p>
     * resource:
     * paperName.keyId.pem
     * keyId.pem
     * paperName.pem
     *
     * @param paperName paperName
     * @param keyId     keyId
     * @return privateKey
     */
    @Override
    public byte[] getPrivateKey(String paperName, String keyId) {
//        byte[] bytes = COMPATIBILITY_LOADER.get().getPrivateKey(paperName);
//        if (bytes != null) {
//            //noinspection deprecation
//            log.warn("{} deprecated. use {} plz.", RSAKeyStore.class.getName(), RSA_KeyStore.class.getName());
//            return bytes;
//        }
        try {
            return loadKey(
                    getConfigKeys(paperName, keyId, "privateKey"),
                    getResourceList(paperName, keyId, "pem")
            );
        } catch (IOException e) {
            throw new ConcreteException(SIGNATURE_KEY_LOAD_FAILED, paperName, keyId, e.getLocalizedMessage());
        }
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
     * keyId.crt
     * paperName.crt
     *
     * @param paperName paperName
     * @param keyId     keyId
     * @return publicKey
     */
    @Override
    public byte[] getPublicKey(String paperName, String keyId) {
//        byte[] bytes = COMPATIBILITY_LOADER.get().getPublicKey(paperName, keyId);
//        if (bytes != null) {
//            log.warn("{} deprecated. use {} plz.", RSAKeyStore.class.getName(), RSA_KeyStore.class.getName());
//            return bytes;
//        }

        try {
            return loadKey(
                    getConfigKeys(paperName, keyId, "publicKey"),
                    getResourceList(paperName, keyId, "crt")
            );
        } catch (IOException e) {
            throw new ConcreteException(SIGNATURE_KEY_LOAD_FAILED, paperName, keyId, e.getLocalizedMessage());
        }
    }

    @Override
    public boolean accept(ServiceContext param) {
        return true;
    }
}
