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
