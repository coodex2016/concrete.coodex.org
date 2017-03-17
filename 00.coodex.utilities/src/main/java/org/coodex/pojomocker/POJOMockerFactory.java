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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author davidoff
 *
 */
public abstract class POJOMockerFactory {

   private final Collection<AbstractClassInstanceMocker> mockerProducts = new ArrayList<AbstractClassInstanceMocker>();

   private static final Collection<AbstractClassInstanceMocker> mockers = new ArrayList<AbstractClassInstanceMocker>();

   private static final AbstractClassInstanceMocker defaultClassMocker = new CI_DefaultMocker();

   private final AbstractClassInstanceMocker findMockerFromList(Class<?> clz,
         Collection<AbstractClassInstanceMocker> list) {
      for (AbstractClassInstanceMocker mocker : list) {
         if (mocker.access(clz))
            return mocker;
      }
      return null;
   }

   /**
    * 注册一个工厂特定的ClassMocker
    * 
    * @param mocker
    */
   protected final void registMocker(AbstractClassInstanceMocker mocker) {
      if (mocker != null && !mockerProducts.contains(mocker))
         mockerProducts.add(mocker);
   }

   /**
    * 注册全局的ClassMocker
    * 
    * @param mocker
    */
   static void registGlobalMocker(AbstractClassInstanceMocker mocker) {
      if (mocker != null && !mockers.contains(mocker))
         mockers.add(mocker);
   }

   public final AbstractClassInstanceMocker getClassInstanceMocker(Class<?> clz) {
      AbstractClassInstanceMocker mocker = findMockerFromList(clz, mockers);
      if (mocker == null)
         mocker = findMockerFromList(clz, mockerProducts);
      if (mocker == null)
         mocker = defaultClassMocker;
      return mocker;
   }

}
