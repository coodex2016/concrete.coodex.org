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

import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.coodex.concrete.core.signature.SignUtil.getString;

public class HMAC_KeyStoreDefaultImpl implements HMAC_KeyStore {

    private final static Logger log = LoggerFactory.getLogger(HMAC_KeyStoreDefaultImpl.class);

    @SuppressWarnings("deprecation")
    private static ServiceLoader<HmacKeyStore> COMPATIBILITY_LOADER = new ServiceLoaderImpl<HmacKeyStore>(
            new HmacKeyStore() {
                /**
                 * 优先级
                 * hmacKey.paperName.keyId
                 * hmacKey.paperName
                 * hmacKey.keyId
                 * hmacKey
                 *
                 * @param paperName paperName
                 * @param keyId keyId
                 * @return
                 */
                @Override
                public byte[] getHmacKey(String paperName, String keyId) {
                    String s = getHmacKeyStr(paperName, keyId);
                    return s == null ? null : s.getBytes();
                }
            }) {
    };

    private static String getHmacKeyStr(String paperName, String keyId) {
        if (Common.isBlank(keyId))
            return getString("hmacKey", paperName, null);
        String key = null;
        if (!Common.isBlank(paperName)) {
            key = getString("hmacKey." + paperName + "." + keyId, null, null);
            if (key == null)
                key = getString("hmacKey." + paperName, null, null);
        }
        if (key == null)
            key = getString("hmacKey", keyId, null);

        return key == null ? getString("hmacKey", null, null) : key;
    }

    @Override
    public byte[] getHmacKey(String paperName, String keyId) {
        byte[] bytes = COMPATIBILITY_LOADER.get().getHmacKey(paperName, keyId);
        if (bytes != null) {
            log.warn("{} deprecated. use {} plz.", HmacKeyStore.class.getName(), HMAC_KeyStore.class.getName());
            return bytes;
        }
        String s = getHmacKeyStr(paperName, keyId);
        return s == null ? null : s.getBytes();
    }

    @Override
    public boolean accept(ServiceContext param) {
        return true;
    }
}
