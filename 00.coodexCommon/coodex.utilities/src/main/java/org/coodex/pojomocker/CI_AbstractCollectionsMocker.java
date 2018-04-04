/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

import java.util.Collection;

/**
 * @author davidoff
 *
 */
@Deprecated
public abstract class CI_AbstractCollectionsMocker extends
      AbstractUnmockFieldMocker {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#newInstance
    * (java.lang.Class, org.coodex.pojomock.refactoring.MockContext)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected final <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException {

      return (T) createCollectionInstance(context);
   }

   @Override
   public POJOMockInfo getDefaultMockInfo() {

      POJOMockInfo pmi = super.getDefaultMockInfo();
      pmi.setMax(10);
      pmi.setMin(1);
      return pmi;
   }

   protected abstract Collection<?> createCollectionInstance(MockContext context);

}
