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

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.IF;
import org.coodex.util.Common;
import org.coodex.util.DigestHelper;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

import java.util.Arrays;

import static org.coodex.concrete.core.signature.SignUtil.getString;

/**
 * Created by davidoff shen on 2017-04-24.
 */
public class HmacPen extends AbstractIronPen {

    private static final HmacKeyStore DEFAULT_KEY_STORE = new HmacKeyStore() {
        /**
         * 优先级
         * hmacKey.paperName.keyId
         * hmacKey.paperName
         * hmacKey.keyId
         * hmacKey
         *
         * @param paperName
         * @param keyId
         * @return
         */
        @Override
        public byte[] getHmacKey(String paperName, String keyId) {
            String s = getHmacKeyStr(paperName, keyId);
            return s == null ? null : s.getBytes();
        }

        private String getHmacKeyStr(String paperName, String keyId) {
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
    };
    private static final ServiceLoader<HmacKeyStore> HMAC_KEY_STORE_PROVIDERS = new ServiceLoaderImpl<HmacKeyStore>(DEFAULT_KEY_STORE) {
    };


    HmacPen(String paperName) {
        super(paperName);
    }


    private byte[] getHmacKey(String keyId) {
        return HMAC_KEY_STORE_PROVIDERS.getInstance().getHmacKey(paperName, keyId);
    }

    @Override
    public byte[] sign(byte[] content, String algorithm, String keyId) {
        return sign(content, IF.isNull(getHmacKey(keyId)
                , ErrorCodes.SIGNING_FAILED, "invalid HMAC Key"), algorithm);
    }

    private byte[] sign(byte[] content, byte[] key, String algorithm) {
        try {
            return DigestHelper.hmac(content, key, algorithm);
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }


    @Override
    public boolean verify(byte[] content, byte[] signature, String algorithm, String keyId) {
        return Arrays.equals(signature,
                sign(content,
                        IF.isNull(getHmacKey(keyId),
                                ErrorCodes.SIGNATURE_VERIFICATION_FAILED,
                                "invalid HMAC Key"),
                        algorithm));
    }
}

