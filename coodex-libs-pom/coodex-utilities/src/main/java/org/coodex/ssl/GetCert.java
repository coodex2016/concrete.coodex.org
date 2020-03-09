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

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public final class GetCert {
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

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
        while (storePath.endsWith("/") || storePath.endsWith("\\")) {
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

    private static class SavingTrustManager implements X509TrustManager {

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
        }
    }

}
