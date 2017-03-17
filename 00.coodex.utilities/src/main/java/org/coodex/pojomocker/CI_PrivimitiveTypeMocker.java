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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author davidoff
 *
 */
public class CI_PrivimitiveTypeMocker extends AbstractUnmockFieldMocker {

   private static Logger log = LoggerFactory
         .getLogger(CI_PrivimitiveTypeMocker.class);

   private static final Class<?>[] PRIVIMITIVE_TYPES = new Class<?>[] {
         Boolean.class, Byte.class, Character.class, Short.class,
         Integer.class, Long.class, Float.class, Double.class };

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#access(java
    * .lang.Class)
    */
   @Override
   protected boolean access(Class<?> clz) {
      return clz.isPrimitive() || Common.inArray(clz, PRIVIMITIVE_TYPES);
   }


   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#newInstance
    * (java.lang.Class, org.coodex.pojomock.refactoring.MockContext)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T newInstance(Class<T> clz, MockContext context) {
      if (isByte(clz)) {
         return (T) Byte.valueOf((byte) mockInt(context));

      } else if (isChar(clz)) {
         return (T) Character.valueOf((char) mockInt(context));

      } else if (isBoolean(clz)) {
         return (T) Boolean.valueOf(mockBool(context));

      } else if (isShort(clz)) {
         return (T) Short.valueOf((short) mockInt(context));

      } else if (isInt(clz)) {
         return (T) Integer.valueOf(mockInt(context));

      } else if (isLong(clz)) {
         return (T) Long.valueOf(mockInt(context));

      } else if (isFloat(clz)) {
         return (T) Float.valueOf((float) mockDouble(context));

      } else if (isDouble(clz)) {
         return (T) Double.valueOf(mockDouble(context));

      } else
         return null;
   }

   /**
    * 基于上下文模拟一个整型数
    *
    * @param context
    * @return
    */
   private int mockInt(MockContext context) {
      POJOMockInfo pmi = context.getMockInfo();
      Object mockObj = context.getInstance();
      try {
         if (!"".equals(pmi.getSizeOf()) && mockObj != null) {
            Field fieldTarget = mockObj.getClass().getDeclaredField(
                  pmi.getSizeOf());
            fieldTarget.setAccessible(true);

            Class<?> clz = fieldTarget.getClass();
            Object collectionObj = fieldTarget.get(mockObj);
            if (collectionObj == null)
               return 0;
            else if (clz.isArray()) {
               return Array.getLength(collectionObj);
            } else if (Collection.class.isAssignableFrom(clz)) {
               return ((Collection<?>) collectionObj).size();
            } else if (CharSequence.class.isAssignableFrom(clz)) {
               return ((CharSequence) collectionObj).length();
            }
         }
      } catch (Throwable e) {
         log.debug("mock failed. {}", e.getLocalizedMessage(), e);
      }
      return Common.random(pmi.getMin(), pmi.getMax());
   }

   /**
    * 1/2几率true/false
    * 
    * @param context
    * @return
    */
   private boolean mockBool(MockContext context) {
      return Math.random() < 0.5;
   }

   /**
    * 基于上下文模拟一个双精度数
    * 
    * @param MockContext
    * @return
    */
   private double mockDouble(MockContext context) {
      POJOMockInfo pmi = context.getMockInfo();
      return Math.random() + Common.random(pmi.getMin(), pmi.getMax() - 1);
   }

   private boolean isInt(Class<?> clz) {
      return clz == int.class || clz == Integer.class;
   }

   private boolean isLong(Class<?> clz) {
      return clz == long.class || clz == Long.class;
   }

   private boolean isChar(Class<?> clz) {
      return clz == char.class || clz == Character.class;
   }

   private boolean isByte(Class<?> clz) {
      return clz == byte.class || clz == Byte.class;
   }

   private boolean isShort(Class<?> clz) {
      return clz == short.class || clz == Short.class;
   }

   private boolean isBoolean(Class<?> clz) {
      return clz == boolean.class || clz == Boolean.class;
   }

   private boolean isFloat(Class<?> clz) {
      return clz == float.class || clz == Float.class;
   }

   private boolean isDouble(Class<?> clz) {
      return clz == double.class || clz == Double.class;
   }

}
