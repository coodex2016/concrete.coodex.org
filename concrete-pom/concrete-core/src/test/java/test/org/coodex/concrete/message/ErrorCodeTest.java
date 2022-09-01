/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.concrete.message;

import org.coodex.concrete.api.ErrorCode;
import org.coodex.concrete.common.ConcreteLocaleProvider;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.util.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class ErrorCodeTest {


    private void testWithLocale(Locale locale) throws IllegalAccessException {
        Class<ErrorCodes> c = ErrorCodes.class;
        for (Field f : c.getDeclaredFields()) {
            if (Objects.equals(f.getType(), int.class)
                    && Modifier.isPublic(f.getModifiers())
                    && Modifier.isFinal(f.getModifiers())
                    && Modifier.isStatic(f.getModifiers())) {
                int x = (int) f.get(null);
                String key =
                        "message.concrete." + Optional.ofNullable(f.getAnnotation(ErrorCode.Key.class)).map(ErrorCode.Key::value).orElse(String.valueOf(x));
                Assertions.assertEquals(
                        I18N.translate(key, locale),
                        ErrorMessageFacade.getTemplate(x)
                );
            }
        }
    }

    @Test
    public void test() throws IllegalAccessException {
        ConcreteLocaleProvider.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        testWithLocale(Locale.SIMPLIFIED_CHINESE);
        ConcreteLocaleProvider.setDefaultLocale(Locale.US);
        testWithLocale(Locale.US);
    }
}
