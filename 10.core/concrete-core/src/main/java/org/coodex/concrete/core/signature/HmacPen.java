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

import org.coodex.concrete.common.*;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.DigestHelper;
import org.coodex.util.I18N;

import java.util.Arrays;

/**
 * Created by davidoff shen on 2017-04-24.
 */
public class HmacPen extends AbstractIronPen {


    private static final AcceptableServiceLoader<ServiceContext, HMAC_KeyStore> HMAC_KEY_STORE_PROVIDERS =
            new AcceptableServiceLoader<ServiceContext, HMAC_KeyStore>(
                    new HMAC_KeyStoreDefaultImpl()
            ) {
            };

    HmacPen(String paperName) {
        super(paperName);
    }


    private byte[] getHmacKey(String keyId) {
        return HMAC_KEY_STORE_PROVIDERS
                .select(ConcreteContext.getServiceContext())
                .getHmacKey(paperName, keyId);
    }

    @Override
    public byte[] sign(byte[] content, String algorithm, String keyId) {
        return sign(content, IF.isNull(getHmacKey(keyId)
                , ErrorCodes.SIGNING_FAILED, I18N.translate("sign.invalidHMACKey")), algorithm);
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
                                I18N.translate("sign.invalidHMACKey")),
                        algorithm));
    }
}

