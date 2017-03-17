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

/**
 * 
 */
package org.coodex.pojomocker;

import java.lang.reflect.Type;

/**
 * 基于Type的mocker，用来实现Class/TypeVariant/ParameterizedType各自的模拟
 * 
 * @author davidoff
 *
 */
public abstract class AbstractTypeBasedMocker<T extends Type> {

   protected abstract Class<?> getTypeClass();

   final protected static Object mockByType(Type type)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      return POJOMocker.$mock(type);
   }

   @SuppressWarnings("unchecked")
   final Object mock(Type t, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      if (getTypeClass().isAssignableFrom(t.getClass()))
         return $mock((T) t, context);
      else
         throw new UnsupportedTypeException(t);
   }

   protected abstract Object $mock(T type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException,UnableMockException;

}
