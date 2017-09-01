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

package test.org.coodex.intf.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class ExampleInterceptor2 extends AbstractInterceptor {

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        System.out.println(22220);
        super.before(context, joinPoint);
    }

    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        System.out.println(22222);
        return super.after(context, joinPoint, result);
    }

    //    @Override
//    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
//        System.out.println(222220);
//        try {
//            return super.around(context, joinPoint);
//        } finally {
//            System.out.println(222222);
//        }
//    }
}
