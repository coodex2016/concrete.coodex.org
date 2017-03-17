/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.security.impl;

import org.coodex.security.SecretContext;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;

/**
 * Created by davidoff shen on 2017-02-07.
 */
public class RSASecretContext implements SecretContext {

    private long keyCreated;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private int keySize = 1024;

    public RSASecretContext(int keySize) {
        this.keySize = keySize;
    }

    public RSASecretContext() {
        this(1024);
    }


    @Override
    public synchronized PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public synchronized void reset() {
        keyCreated = System.currentTimeMillis();

    }

    @Override
    public long keyAge() {
        return System.currentTimeMillis() - keyCreated;
    }

    @Override
    public byte[] decrypt(byte[] cipherContent) {
        PrivateKey key;
        synchronized (this) {
            key = privateKey;
        }
        RSAPrivateKey o;
        ////
        return new byte[0];
    }

    @Override
    public byte[] encrypt(byte[] content) {
        PublicKey key = getPublicKey();
        ////
        return new byte[0];
    }
}
