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

package org.coodex.concrete.jaxrs.client;

import okhttp3.*;
import okhttp3.internal.Util;
import okhttp3.internal.platform.Platform;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import static org.coodex.concrete.common.ConcreteContext.SUBJOIN;
import static org.coodex.concrete.jaxrs.JaxRSHelper.HEADER_ERROR_OCCURRED;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class OkHttp3Invoker extends AbstractRemoteInvoker {

    private final static Logger log = LoggerFactory.getLogger(OkHttp3Invoker.class);

    private final OkHttpClient client;

    public OkHttp3Invoker(String domain, SSLContext sslContext) {
        super(domain);
        OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(new CookieManager());

        if (sslContext != null) {
            builder.sslSocketFactory(sslContext.getSocketFactory(),
                    Platform.get().trustManager(sslContext.getSocketFactory()));
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }

        client = builder.build();
    }


    private Request build(String method, Request.Builder builder, RequestBody body) {
        if ("DELETE".equalsIgnoreCase(method))
            return builder.delete(body).build();
        else if ("PUT".equalsIgnoreCase(method))
            return builder.put(body).build();
        else if ("GET".equalsIgnoreCase(method))
            return builder.get().build();
        else
            return builder.post(body).build();

    }


    @Override
    protected Object invoke(String url, Unit unit, Object toSubmit) throws Throwable {
        Request.Builder builder = new Request.Builder().url(url)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
//                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
                .addHeader("Content-Type", "application/json; charset=" + getEncodingCharset());
        
        Subjoin subjoin = SUBJOIN.get();
        if (subjoin != null) {
            for (String key : subjoin.keySet()) {
                builder = builder.addHeader(key, subjoin.get(key));
            }
        }

        RequestBody body = toSubmit == null ?
                Util.EMPTY_REQUEST :
                RequestBody.create(MediaType.parse("application/json; charset=" + getEncodingCharset()),
                        toStr(toSubmit));
        log.debug("request:{} {}", unit.getInvokeType(), url);

        Response response = client.newCall(build(unit.getInvokeType(), builder, body)).execute();
        return processResult(response.code(), response.body().string(), unit,
                response.header(HEADER_ERROR_OCCURRED) != null, url);

    }
}
