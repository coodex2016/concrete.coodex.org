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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author davidoff
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface POJOMock {

   enum MockType {
      STRING_ASCII, STRING_ZHCN, STRING_ALL, STRING_DATETIME, STRING_DATE, STRING_TIME, STRING_NUMBER, STRING_ID
   }

   // ///// String int long

   int min() default 0;

   int max() default 5;

   /**
    * 最大可循环次数
    * 
    * @return
    */
   int maxRecycledCount() default 3;

   MockType type() default MockType.STRING_ZHCN;

   // int
   String sizeOf() default "";

   int[] arraySize() default { -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
         5, 5 };

   Class<? extends POJOMockerFactory> factory() default DefaultPOJOMockerFactory.class;

   /**
    * 当field非空时，是否强制模拟
    * 
    * @return
    */
   boolean forceMock() default true;

}
