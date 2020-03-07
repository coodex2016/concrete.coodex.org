/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.*;
import java.util.Arrays;
import java.util.List;

public class X509TrustManagerImpl implements X509TrustManager {

    private final static Logger log = LoggerFactory.getLogger(X509TrustManagerImpl.class);

    private final X509TrustManager trustManager;
    private final KeyStore keyStore;

    X509TrustManagerImpl(InputStream certFile) throws Exception {
        this.keyStore = this.initKeyStore(certFile);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        this.trustManager = (X509TrustManager) trustManagers[0];
    }

    private KeyStore initKeyStore(InputStream file) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(file);
        trustStore.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        return trustStore;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // no check!!
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            this.trustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException certificateException) {
            CertificateNotYetValidException certificateNotYetValidException = null;
            try {
                X509Certificate[] reorderedChain = this.reorderCertificateChain(chain);
                CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                CertificateFactory factory = CertificateFactory.getInstance("X509");
                CertPath certPath = factory.generateCertPath(Arrays.asList(reorderedChain));
                PKIXParameters params = new PKIXParameters(this.keyStore);
                params.setRevocationEnabled(false);
                validator.validate(certPath, params);
            } catch (CertificateNotYetValidException e) {
                certificateNotYetValidException = e;
            } catch (Throwable th) {
                Throwable caused = th.getCause();
                if (caused instanceof CertificateNotYetValidException) {
                    certificateNotYetValidException = (CertificateNotYetValidException) caused;
                } else {
                    throw certificateException;
                }
            }
            if (certificateNotYetValidException != null) {
                log.warn("certificate not yet valid.", certificateNotYetValidException);
            }
        }

    }

    private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {
        X509Certificate[] reorderedChain = new X509Certificate[chain.length];
        List<X509Certificate> certificates = Arrays.asList(chain);
        int position = chain.length - 1;
        X509Certificate rootCert = this.findRootCert(certificates);
        reorderedChain[position] = rootCert;

        for (X509Certificate cert = rootCert; (cert = this.findSignedCert(cert, certificates)) != null && position > 0; reorderedChain[position] = cert) {
            --position;
        }

        return reorderedChain;
    }

    private X509Certificate findRootCert(List<X509Certificate> certificates) {
        X509Certificate rootCert = null;

        for (X509Certificate cert : certificates) {
            X509Certificate signer = this.findSigner(cert, certificates);
            if (signer == null || signer.equals(cert)) {
                rootCert = cert;
                break;
            }
        }

        return rootCert;
    }

    private X509Certificate findSignedCert(X509Certificate signingCert, List<X509Certificate> certificates) {
        X509Certificate signed = null;

        for (X509Certificate cert : certificates) {
            Principal signingCertSubjectDN = signingCert.getSubjectDN();
            Principal certIssuerDN = cert.getIssuerDN();
            if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                signed = cert;
                break;
            }
        }

        return signed;
    }

    private X509Certificate findSigner(X509Certificate signedCert, List<X509Certificate> certificates) {
        X509Certificate signer = null;

        for (X509Certificate cert : certificates) {
            Principal certSubjectDN = cert.getSubjectDN();
            Principal issuerDN = signedCert.getIssuerDN();
            if (certSubjectDN.equals(issuerDN)) {
                signer = cert;
                break;
            }
        }

        return signer;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
