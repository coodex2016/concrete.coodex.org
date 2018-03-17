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

    static class SavingTrustManager implements X509TrustManager {

        private X509Certificate[] chain;

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
//            tm.checkServerTrusted(chain, authType);
        }
    }

    public static void saveCertificateFromServer(String host, int port, String storePath) throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateEncodingException {

        SSLContext context = SSLContext.getInstance("SSL");
        SavingTrustManager tm = new SavingTrustManager();
        context.init(null, new TrustManager[]{tm}, new SecureRandom());
        SSLSocketFactory factory = context.getSocketFactory();
        System.out
                .println("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            socket.startHandshake();
            socket.close();
        } catch (Throwable th) {
        }
        if (tm.chain == null || tm.chain.length == 0) {
            System.out.println("Could not obtain server certificate chain");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));

        System.out.println();
        System.out.println("Server sent " + tm.chain.length + " certificate(s):");
        System.out.println();
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < tm.chain.length; i++) {
            X509Certificate cert = tm.chain[i];
            System.out.println(" " + (i + 1) + " Subject "
                    + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            System.out.println("   md5     " + toHexString(md5.digest()));
            System.out.println();
        }

        System.out
                .println("Enter certificate to add to trusted keystore or 'q' to quit: [1]");
        String line = reader.readLine().trim();
        int k;
        try {
            k = (line.length() == 0) ? 0 : Integer.parseInt(line) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (k >= tm.chain.length || k < 0) return;

        X509Certificate cert = tm.chain[k];
        String name = host + "." + port;
        while (storePath.endsWith("/") || storePath.endsWith("\\")){
            storePath = storePath.substring(0, storePath.length() - 1);
        }
        int index = 0;
        while (isExists(storePath + File.separatorChar + name + ".cer")) {
            name = host + "." + port + "-" + ++index;
        }
        File x = Common.getNewFile(storePath + File.separatorChar + name + ".cer");
        OutputStream os = new FileOutputStream(x);
        try {
            os.write(cert.getEncoded());
            os.flush();
        } finally {
            os.close();
        }
        System.out.println("certificate saved: " + x.getAbsolutePath());
    }

    private static boolean isExists(String name) {
        File f = new File(name);
        return f.exists();
    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

//    public static void main(String[] args) throws CertificateException, IOException, NoSuchAlgorithmException, KeyManagementException {
//
//        saveCertificateFromServer("docs.oracle.com", 443, "/");
////        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//
////        System.out.println(certificateFactory.generateCertificate(new FileInputStream("E:\\FiddlerRoot.cer")));
//    }

}
