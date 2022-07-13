/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package org.coodex.util.config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TTTT {

    public static Method getMethod(Class<?> objClass, String methodName) throws NoSuchMethodException {
        for (Method method : objClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new NoSuchMethodException();
    }

    public static Object invoke(Object obj, String methodName, Object... args) throws ReflectiveOperationException {
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        overrideField.setAccessible(true);
        Method targetMethod = getMethod(obj.getClass(), methodName);
        overrideField.set(targetMethod, true);
        return targetMethod.invoke(obj, args);
    }

    public static Class<?> getConsumerLambdaParameterType(Consumer<?> consumer) throws ReflectiveOperationException {
        Class<?> consumerClass = consumer.getClass();
        Object constantPool = invoke(consumerClass, "getConstantPool");
        for (int i = (int) invoke(constantPool, "getSize") - 1; i >= 0; --i) {
            try {
                Object member = invoke(constantPool, "getMethodAt", i);
                if (member instanceof Method && ((Method) member).getDeclaringClass() != Object.class) {
                    Method method = (Method) member;
                    return method.getParameterTypes()[0];
                }
            } catch (Exception ignored) {// NOSONAR
                // ignored
            }
        }
        throw new NoSuchMethodException();
    }

    public static Method getLambadaMethod(Object o) throws ReflectiveOperationException {
        Class<?> consumerClass = o.getClass();
        Object constantPool = invoke(consumerClass, "getConstantPool");
        for (int i = (int) invoke(constantPool, "getSize") - 1; i >= 0; --i) {
            try {
                Object member = invoke(constantPool, "getMethodAt", i);
                if (member instanceof Method && ((Method) member).isSynthetic() && ((Method) member).getDeclaringClass() != Object.class) {
                    return (Method) member;
                }
            } catch (Exception ignored) {// NOSONAR
                // ignored
            }
        }
        return null;
//        throw new NoSuchMethodException();
    }

    private static <T> T test2(T t) throws ReflectiveOperationException {
        return test(() -> t);
    }

    private static <T> T test(Supplier<T> supplier) throws ReflectiveOperationException {
//        int i = 0;
//        System.out.println(getLambadaMethod((Supplier<Integer>)()->i));
//        System.out.println(getLambadaMethod((Function<Integer,Boolean>)ii->true));
        System.out.println(getLambadaMethod(supplier));
        return supplier.get();
    }


    public static void main(String[] args) throws ReflectiveOperationException {
        Supplier<Integer> integerSupplier = () -> null;
        Runnable runnable = () -> {
        };
        Callable<String> callable = () -> null;
        System.out.println(getLambadaMethod(integerSupplier));
        System.out.println(getLambadaMethod(runnable));
        System.out.println(getLambadaMethod(callable));

        Object i = test(() -> 1);
        test2(1);

    }


}
