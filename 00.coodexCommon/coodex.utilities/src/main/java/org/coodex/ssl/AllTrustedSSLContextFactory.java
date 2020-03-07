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
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 信任所有证书
 */
public class AllTrustedSSLContextFactory implements SSLContextFactory {

    private final static Logger log = LoggerFactory.getLogger(AllTrustedSSLContextFactory.class);


    @Override
    public SSLContext getSSLContext(String param) throws Throwable {
        SSLContext context = SSLContext.getInstance("SSL");

        context.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // no check!!
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // no check!!
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());

        log.warn("AllTrustedSSLContextFactory NOT safely for data transform");
        return context;
    }

    @Override
    public boolean accept(String param) {
        return !Common.isBlank(param) && "AllTrusted".equalsIgnoreCase(param);
    }
}
