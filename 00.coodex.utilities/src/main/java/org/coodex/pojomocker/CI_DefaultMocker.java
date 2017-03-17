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

/**
 * @author davidoff
 *
 */
public class CI_DefaultMocker extends AbstractClassInstanceMocker {

   @Override
   protected boolean access(Class<?> clz) {
      return true;
   }

   @Override
   protected boolean needCreate(int created, MockContext context) {
      return created <= context.getMockInfo().getMaxRecycledCount();
   }

   @Override
   protected boolean needMockFields(Class<?> clz) {
      String packageName = clz.getPackage().getName();
      return !packageName.startsWith("java");
   }

   @Override
   protected <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException {
      try {

         return clz.newInstance();
      } catch (InstantiationException e) {
         throw new UnsupportedTypeException(clz);
      }
   }

}
