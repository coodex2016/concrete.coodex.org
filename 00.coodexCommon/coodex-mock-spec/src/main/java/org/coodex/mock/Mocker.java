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

package org.coodex.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.ServiceLoader;


public final class Mocker {

    private static final String DEFAULT_PROVIDER_CLASS = Mocker.class.getPackage().getName() + ".CoodexMockerProvider";
    private static boolean instanceLoaded = false;
    private static MockerProvider mockerProviderInstance = null;
    private static Throwable th = null;

    private Mocker() {
    }

    private static MockerProvider getMockerProvider() {
        if (!instanceLoaded) {
            synchronized (Mocker.class) {
                if (!instanceLoaded) {
                    try {
                        Iterator<MockerProvider> serviceLoader = ServiceLoader.load(MockerProvider.class).iterator();
                        if (serviceLoader.hasNext()) {
                            mockerProviderInstance = serviceLoader.next();
                        } else {
                            mockerProviderInstance = (MockerProvider) Class.forName(DEFAULT_PROVIDER_CLASS).newInstance();
                        }
                    } catch (Throwable throwable) {
                        th = throwable;
                    } finally {
                        instanceLoaded = true;
                    }
                }
            }
        }

        if (mockerProviderInstance == null) {
            if (th == null)
                throw new MockException("none provider found.");
            else
                throw new MockException("none provider found. ", th);
        }
        return mockerProviderInstance;
    }

    public static <T> T mock(Class<T> type, Annotation... annotations) {
        return getMockerProvider().mock(type, annotations);
    }

    public static Object mock(Type type, Annotation... annotations) {
        return getMockerProvider().mock(type, annotations);
    }

}
