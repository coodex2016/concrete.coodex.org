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

package org.coodex.ssl;

import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class X509CertsSSLContextFactory implements SSLContextFactory {

    private final static Logger log = LoggerFactory.getLogger(X509CertsSSLContextFactory.class);


    private static final String CERT_PATH = "certPath:".toLowerCase();
    private static final String CLASS_PATH = "cp:";
//    private static final String ALIAS = "coodex-certs-alias-";

//    private final X509TrustManager x509TrustManager;

    @Override
    public SSLContext getSSLContext(String param) throws Throwable {
        String certPath = Common.trim(param.substring(CERT_PATH.length()), ',', ' ', ':', ';');
        Set<String> allPath = Common.arrayToSet(Common.toArray(certPath, ";", new String[0]));
        SSLContext sslContext = SSLContext.getInstance("TLSV1.2");
        List<X509TrustManager> trustManagers = new ArrayList<X509TrustManager>();

//        KeyStore trusted = KeyStore.getInstance(KeyStore.getDefaultType());
//        trusted.load(null); // 初始化一个空库

//        int index = 1;

        for (String path : allPath) {
            if (Common.isBlank(path)) continue;
            if (path.toLowerCase().startsWith(CLASS_PATH)) {
                String[] certs = Common.toArray(path.substring(CLASS_PATH.length()), ",", new String[0]);
                for (String cert : certs) {
                    InputStream inputStream = Common.getResource(cert).openStream();
                    try {
                        trustManagers.add(new X509TrustManagerImpl(inputStream));
                    } finally {
                        inputStream.close();
                    }
//                    loadCertFromInputStream(trusted, getCertFromResource(cert), ALIAS + index++);
                }
            } else {
                InputStream inputStream = getCertFromFile(path);
                if (inputStream == null) continue;
                try {
                    trustManagers.add(new X509TrustManagerImpl(inputStream));
                } finally {
                    inputStream.close();
                }
//                loadCertFromInputStream(trusted, getCertFromFile(path), ALIAS + index++);
            }
        }

        //返回trustManager
//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
//        trustManagerFactory.init(trusted);

        sslContext.init(null, trustManagers.toArray(new X509TrustManager[0]),null);

        return sslContext;
    }

    private void loadCertFromInputStream(KeyStore keyStore, InputStream inputStream, String alias) throws CertificateException, KeyStoreException, IOException {
        if (inputStream == null) return;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            keyStore.setCertificateEntry(alias,
                    certificateFactory.generateCertificate(inputStream));
        } finally {
            inputStream.close();
        }
    }

    private InputStream getCertFromResource(String path) throws IOException {
        if (Common.isBlank(path)) return null;
        URL url = Common.getResource(Common.trim(path, ' ', '/'));
        if (url == null) {
            log.warn("cert resource not found: classpath:{}", path);
            return null;
        } else {
            return url.openStream();
        }
    }


    private InputStream getCertFromFile(String path) throws IOException {
        if (Common.isBlank(path)) return null;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return new FileInputStream(file);
        } else {
            log.warn("cert file {} not exists.", path);
            return null;
        }
    }

    @Override
    public boolean accept(String param) {
        return !Common.isBlank(param) && param.toLowerCase().startsWith(CERT_PATH);
    }
}
