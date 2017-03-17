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

package org.coodex.practice.jaxrs.impl;

import com.alibaba.fastjson.JSON;
import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;
import org.coodex.concrete.jaxrs.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public class ClientInterceptorTest extends AbstractInterceptor {

    private final static Logger log = LoggerFactory.getLogger(ClientInterceptorTest.class);

    @Override
    public int getOrder() {
        return 0;
    }



    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        log.debug("before Method: {} ", context.getDeclaringMethod().getName());
        MethodInvocation invocation = joinPoint;
        if (invocation.getMethod().getName().equals("bigStringTest")) {
            log.debug("{}", Client.getUnitFromContext(context,joinPoint).getInvokeType());
            log.debug("change parameter 2 value to hhddjjjdjdjd");
            Object[] args = joinPoint.getArguments();
            args[1] = "hhddjjjdjdjd";
        }
        super.before(context, joinPoint);
    }


    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        log.debug("after Method: {}, result: {} ", context.getDeclaringMethod().getName(), JSON.toJSONString(result));
        return super.after(context, joinPoint, result);
    }
}
