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

package org.coodex.concrete.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.coodex.concrete.core.intercept.AbstractInterceptor;
import org.springframework.core.Ordered;

/**
 * Created by davidoff shen on 2016-09-01.
 */
public abstract class AbstractConcreteAspect extends AbstractInterceptor implements Ordered {
    public static final String ASPECT_POINT = "target(org.coodex.concrete.api.ConcreteService) && execution(public * *(..))";


    @Around(ASPECT_POINT)
    public Object weaverPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        return invoke(AspectJHelper.proceedJoinPointToMethodInvocation(joinPoint));
    }
    
}
