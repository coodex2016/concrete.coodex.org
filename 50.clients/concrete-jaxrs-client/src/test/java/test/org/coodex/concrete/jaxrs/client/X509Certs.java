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

package test.org.coodex.concrete.jaxrs.client;

import org.coodex.concrete.jaxrs.client.impl.X509CertsSSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by davidoff shen on 2017-03-27.
 */
public class X509Certs {
    public static void main(String[] args) throws Throwable {
        SSLContext context = new X509CertsSSLContextFactory().getSSLContext("https://docs.oracle.com");
        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket("docs.oracle.com", 443);
        socket.setSoTimeout(10000);
        socket.startHandshake();
        socket.close();
    }
}
