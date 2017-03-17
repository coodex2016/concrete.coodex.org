/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * 
 */
package org.coodex.pojomocker;

import org.coodex.util.Common;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * 泛型数组模拟器
 * 
 * @author davidoff
 *
 */
public class GenericArrayTypeMocker extends
      AbstractTypeBasedMocker<GenericArrayType> {

   @Override
   protected Class<?> getTypeClass() {
      return GenericArrayType.class;
   }

   @Override
   protected Object $mock(GenericArrayType type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException, UnableMockException {
      POJOMockInfo pmi = context.getMockInfo();
      int level = context.getArrayLevel();
      int[] arraySize = pmi.getArraySize();
      int size = level < arraySize.length ? arraySize[level] : Common.random(
            pmi.getMin(), pmi.getMax());
      if (size < 0) {
         size = Common.random(pmi.getMin(), pmi.getMax());
      }

      Type componentType = type.getGenericComponentType();
      context.arrayLevelAdd();
      try {
         Object array = null;
         for (int i = 0; i < size; i++) {
            MockContextHelper.enter();
            try {
               Object element = mockByType(componentType);
               if (array == null)
                  array = Array.newInstance(element.getClass(), size);

               Array.set(array, i, element);
            } finally {
               MockContextHelper.leave();
            }
         }
         return array;
      } finally {
         context.arrayLevelReduce();
      }
   }

}
