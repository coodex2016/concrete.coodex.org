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

import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.signature.SignUtil.TAG_SIGNATRUE;
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
                 * @return hmacKey
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


    private static String getHmacKeyStr(String paperName, String keyId, String module) {
        List<String> namespace = new ArrayList<>();
        namespace.add(module == null ? TAG_SIGNATRUE : TAG_CLIENT);
        if (module != null)
            namespace.add(module);
        namespace.add(getAppSet());
        String[] namespaceArray = namespace.toArray(new String[0]);
        boolean blankPaper = Common.isBlank(paperName);
        String hmacKeyProperty = module == null ? "hmacKey" : "signature.hmacKey";
        String key = null;
        if (Common.isBlank(keyId)) {
            if (!blankPaper) {
                key = Config.get(String.format("%s.%s", hmacKeyProperty, paperName), namespaceArray);
            }
            if (key == null) {
                key = Config.get(hmacKeyProperty, namespaceArray);
            }
        } else {
            key = Config.get(String.format("%s.%s", hmacKeyProperty, (blankPaper ? "" : (paperName + ".")) + keyId), namespaceArray);
            if (key == null) {
                key = Config.get(hmacKeyProperty + (blankPaper ? "" : ("." + paperName)), namespaceArray);
            }
        }
        return key;
    }

    private String getModule() {
        ServiceContext context = ConcreteContext.getServiceContext();
        return context instanceof ClientSideContext ?
                ((ClientSideContext) context).getDestination().getIdentify() :
                null;
    }

    @Override
    public byte[] getHmacKey(String paperName, String keyId) {
        String s = getHmacKeyStr(paperName, keyId, getModule());
        if (s != null) {
            return s.getBytes();
        }
        //noinspection deprecation
        log.warn("{} deprecated. use {} plz.", HmacKeyStore.class.getName(), HMAC_KeyStore.class.getName());
        return COMPATIBILITY_LOADER.get().getHmacKey(paperName, keyId);

    }

    @Override
    public boolean accept(ServiceContext param) {
        return true;
    }
}
