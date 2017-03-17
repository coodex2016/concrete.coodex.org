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


/**
 * Class类型模拟器
 * 
 * @author davidoff
 *
 */
public class ClassTypeMocker extends AbstractTypeBasedMocker<Class<?>> {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractTypeBasedMocker#getTypeClass()
    */
   @Override
   protected Class<?> getTypeClass() {
      return Class.class;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractTypeBasedMocker#$mock(java.lang
    * .reflect.Type, org.coodex.pojomock.refactoring.MockContext)
    */
   @Override
   protected Object $mock(Class<?> type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException, UnableMockException {

      return context.getFactory().getClassInstanceMocker(type)
            .mockInstance(type, context, null);
   }

}
