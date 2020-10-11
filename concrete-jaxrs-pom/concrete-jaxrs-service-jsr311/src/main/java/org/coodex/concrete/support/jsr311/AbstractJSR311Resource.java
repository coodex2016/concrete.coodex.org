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

package org.coodex.concrete.support.jsr311;

import org.coodex.concrete.jaxrs.AbstractJAXRSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;


/**
 * Created by davidoff shen on 2016-11-24.
 */
public abstract class AbstractJSR311Resource<T>
        extends AbstractJAXRSResource<T> {

    private final static Logger log = LoggerFactory.getLogger(AbstractJSR311Resource.class);

    @Override
    protected int getMethodStartIndex() {
        return 1;
    }

    private void setPriority(int priority) {
        try {
            Thread.currentThread().setPriority(priority);
        } catch (Throwable th) {
            log.warn("Unable set priority: {} ", th.getLocalizedMessage());
        }
    }


    /**
     * 由于javassist无法创建匿名类，因此利用反射找到指定方法进行调用
     * <p>
     * 已知问题：重载且参数数量相同的无法区分
     * <p>
     *
     * @param methodName methodName
     * @param params     params
     * @return 调用实际服务
     */
    protected Object __execute(final String methodName, final String tokenId, final Object... params) {


        final Method method = findMethod(methodName, null);
        int currentThreadPriority = Thread.currentThread().getPriority();
        int methodPriority = getPriority(method);

        try {
            if (currentThreadPriority != methodPriority) {
                setPriority(methodPriority);
            }
            return invokeByTokenId(tokenId, method, params);
        } finally {
            if (currentThreadPriority != Thread.currentThread().getPriority())
                setPriority(currentThreadPriority);
        }
    }


    @Override
    protected Response.ResponseBuilder textType(Response.ResponseBuilder builder) {
        return builder.type(MediaType.TEXT_PLAIN_TYPE);
    }

    @Override
    protected Response.ResponseBuilder jsonType(Response.ResponseBuilder builder) {
        return builder.type(MediaType.APPLICATION_JSON_TYPE);
    }

    protected Object execute(final String methodName, final String tokenId, Object... objects) {
        return __execute(methodName, tokenId, objects);
    }


}
