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
import org.coodex.util.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.coodex.concrete.core.signature.RSA_KeyStoreDefaultImpl.*;
import static org.coodex.concrete.core.signature.SignUtil.getString;

/**
 * base64
 * <p>
 * Created by davidoff shen on 2017-04-24.
 */
@Deprecated
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
     * keyId.pem
     * paperName.pem
     *
     * @param paperName
     * @return
     */
    @Override
    public byte[] getPrivateKey(String paperName) {
        try {
            String keyId = getString("keyId", paperName, null);
            return loadKey(
//                    Arrays.asList("rsa.privateKey." + paperName + "." + keyId,
//                            "rsa.privateKey." + paperName,
//                            "rsa.privateKey." + keyId,
//                            "rsa.privateKey"),
                    getConfigKeys(paperName, keyId, "privateKey"),
//                    Arrays.asList(paperName + "." + keyId + ".pem", keyId + ".pem", paperName + ".pem")
                    getResourceList(paperName, keyId, "pem")
            );
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
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
     * @param paperName
     * @param keyId
     * @return
     */
    @Override
    public byte[] getPublicKey(String paperName, String keyId) {
        try {
            return loadKey(
//                    Arrays.asList("rsa.publicKey." + paperName + "." + keyId,
//                    "rsa.publicKey." + paperName,
//                    "rsa.publicKey." + keyId,
//                    "rsa.publicKey"),
//                    Arrays.asList(paperName + "." + keyId + ".crt", keyId + ".crt", paperName + ".crt")
                    getConfigKeys(paperName, keyId, "publicKey"),
                    getResourceList(paperName, keyId, "crt")
            );
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }
}
