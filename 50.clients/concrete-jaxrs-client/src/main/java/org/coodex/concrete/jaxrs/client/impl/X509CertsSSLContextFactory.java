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

package org.coodex.concrete.jaxrs.client.impl;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.client.SSLContextFactory;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * 根据concrete.properties中trusted.certs.path.domain.port配置的
 * Created by davidoff shen on 2017-03-27.
 */
@Deprecated
public class X509CertsSSLContextFactory implements SSLContextFactory {
    private final static Logger log = LoggerFactory.getLogger(X509CertsSSLContextFactory.class);


    @Override
    public SSLContext getSSLContext(String domain) throws Throwable {
        domain = getDomainName(domain);
        if (domain == null) return null;
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, getTrustManager(domain), new SecureRandom());
        return context;
    }

    private String getDomainName(String domain) {
        try {
            URL url = new URL(domain);
            return url.getHost() + (url.getPort() == -1 ? "" : ("." + url.getPort()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    private TrustManager[] getTrustManager(String domain) throws Throwable {
        String s = ConcreteHelper.getProfile().getString("trusted.certs.path." + domain,
                ConcreteHelper.getProfile().getString("trusted.certs.path"));
        if (s != null) {
            KeyStore trusted = KeyStore.getInstance(KeyStore.getDefaultType());
            trusted.load(null); // 初始化一个空库

            //加载证书
            loadCertificates(trusted, s);
            //返回trustManager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trusted);
            return trustManagerFactory.getTrustManagers();
        }
        return null;
    }

    private void loadCertificates(KeyStore keyStore, String path) throws IOException, URISyntaxException, CertificateException {

        while (path.startsWith("\\") || path.startsWith("/")) {
            path = path.substring(1);
        }

        Enumeration<URL> certFiles = this.getClass().getClassLoader().getResources(path);
        if (certFiles == null) return;
        while (certFiles.hasMoreElements()) {
            String resource = URLDecoder.decode(
                    certFiles.nextElement().getFile().replace("+", "%2B")/* 缺陷？ */,
                    System.getProperty("file.encoding"));

            if (resource.indexOf('!') >= 0) {
                // TODO: load from jar
            } else {
                try {
                    loadFromPath(keyStore, resource);
                } catch (Throwable t) {
                    log.debug("error on load: {}", t.getLocalizedMessage());
                }
            }
        }

    }

    private void loadFromPath(KeyStore keyStore, String resource) throws CertificateException, IOException {
        File file = new File(resource);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        loadFromFile(file, keyStore, certificateFactory);
    }

    private void loadFromFile(File file, KeyStore keyStore, CertificateFactory certificateFactory) throws IOException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (!f.isDirectory()) // 不遍历
                    loadFromFile(f, keyStore, certificateFactory);
            }
        } else {
            InputStream fis = new FileInputStream(file);
            try {
                String alias = file.getName();
                int index = 0;
                while (keyStore.getCertificate(alias) != null) {
                    alias = file.getName() + '-' + ++index;
                }
                keyStore.setCertificateEntry(alias,
                        certificateFactory.generateCertificate(fis));
                log.info("certificate loaded: {} from {}", alias, file.getAbsolutePath());
            } catch (Throwable e) {
                log.debug("unable load cert file: {}, {}", file.getAbsolutePath(), e.getLocalizedMessage());
            } finally {
                fis.close();
            }
        }
    }





}
