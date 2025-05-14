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

import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.RSACommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by davidoff shen on 2017-04-24.
 */
public class RSAPen extends AbstractIronPen {
    private final static Logger log = LoggerFactory.getLogger(RSAPen.class);


    private static final LazySelectableServiceLoader<ServiceContext, RSA_KeyStore> RSA_KEY_STORE_PROVIDERS =
            new LazySelectableServiceLoader<ServiceContext, RSA_KeyStore>(new RSA_KeyStoreDefaultImpl()) {
            };

    RSAPen(String paperName) {
        super(paperName);
    }

    private String nullToDefault(String algorithm) {
        return Common.isBlank(algorithm) ? "SHA256withRSA" : algorithm;
    }

    @Override
    public byte[] sign(byte[] content, String algorithm, String keyId) {
        try {
            return RSACommon.sign(
                    RSA_KEY_STORE_PROVIDERS
                            .select(ConcreteContext.getServiceContext())
                            .getPrivateKey(paperName, keyId),
                    content, nullToDefault(algorithm));
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }

    @Override
    public boolean verify(byte[] content, byte[] signature, String algorithm, String keyId) {
        try {
            return RSACommon.verify(RSA_KEY_STORE_PROVIDERS
                            .select(ConcreteContext.getServiceContext())
                            .getPublicKey(paperName, keyId),
                    content, signature, nullToDefault(algorithm));
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }
}
