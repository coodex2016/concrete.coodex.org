/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.mock.spring.webmvc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.coodex.mock.Mocker;

import java.lang.reflect.Method;
import java.util.Set;

public class MockUtil {

    private static boolean except(Class clz, Set<Class> classSet) {
        if (classSet == null) return false;
        return classSet.contains(clz);
    }

    public static Object mockMethod(ProceedingJoinPoint proceedingJoinPoint, Set<Class> excepted) throws Throwable {
        CodeSignature func = (CodeSignature) proceedingJoinPoint.getSignature();
        Class methodClass = func.getDeclaringType();
        if (except(methodClass, excepted)) return proceedingJoinPoint.proceed();
        //noinspection unchecked
        Method method = methodClass.getMethod(func.getName(), func.getParameterTypes());
        if (method.getAnnotation(MockIgnore.class) != null) return proceedingJoinPoint.proceed();
        return Mocker.mockMethod(method, methodClass);
    }
}
