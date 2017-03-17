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

import org.coodex.util.Common;
import org.coodex.util.TypeHelper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

/**
 * 带参泛型模拟器
 * 
 * @author davidoff
 *
 */
public class ParameterizedTypeMocker
      extends AbstractTypeBasedMocker<ParameterizedType> {

   @Override
   protected Class<?> getTypeClass() {
      return ParameterizedType.class;
   }

   @Override
   protected Object $mock(ParameterizedType type, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         UnableMockException {

      Class<?> rawType = (Class<?>) type.getRawType();
      // boolean isCollection = Collection.class.isAssignableFrom(rawType);

      // if (!isCollection)
      // context.setTypeContext(type);

      MockContextHelper.enter();
      try {
         context = MockContextHelper.currentContext().addContextType(type);

         // MockContext mc = MockContextHelper.currentContext();
         Type[] types = type.getActualTypeArguments();
         Class<?> declared = (Class<?>) type.getRawType();
         for (int i = 0; i < types.length; i++) {
            if (types[i] instanceof TypeVariable<?>)
               context.addReplace(declared, i, types[i]);
         }

         Object result = context.getFactory().getClassInstanceMocker(rawType)
               .mockInstance(rawType, context, types);

         mockCollection(type, context, rawType, result);
         mockMap(type, context, rawType, result);
         return result;
      } finally {
         MockContextHelper.leave();
      }
   }

   private void mockMap(ParameterizedType type, MockContext context,
         Class<?> rawType, Object result) throws IllegalAccessException,
               UnsupportedTypeException, UnableMockException {
      Type keyType = findMapType(rawType, type, 0);
      Type elementType = findMapType(rawType, type, 1);
      if (elementType != null && keyType != null) {
         @SuppressWarnings("unchecked")
         Map<Object, Object> map = (Map<Object, Object>) result;
         POJOMockInfo pmi = context.getMockInfo();
         int randomSize = Common.random(Math.max(1, pmi.getMin()),
               pmi.getMax());

         for (int i = 0; i < randomSize; i++) {
            MockContextHelper.enter();
            try {
               map.put(mockByType(context, keyType),
                     mockByType(context, elementType));
            } finally {
               MockContextHelper.leave();
            }
         }
      }
   }

   /**
    * @param context
    * @param elementType
    * @return
    * @throws UnsupportedTypeException
    * @throws IllegalAccessException
    * @throws UnableMockException
    */
   private Object mockByType(MockContext context, Type elementType)
         throws UnsupportedTypeException, IllegalAccessException,
         UnableMockException {
      Object element;
      if (elementType instanceof TypeVariable) {
         @SuppressWarnings("unchecked")
         Type t = context.findTypeVariableActurlType(
               (TypeVariable<Class<?>>) elementType);
         element = mockByType(t);
      } else
         element = mockByType(elementType);
      return element;
   }

   /**
    * @param type
    * @param context
    * @param rawType
    * @param result
    * @throws IllegalAccessException
    * @throws UnsupportedTypeException
    * @throws UnableMockException
    */
   private void mockCollection(ParameterizedType type, MockContext context,
         Class<?> rawType, Object result) throws IllegalAccessException,
               UnsupportedTypeException, UnableMockException {
      Type elementType = findElementType(rawType, type);
      if (elementType != null) {
         @SuppressWarnings("unchecked")
         Collection<Object> collection = (Collection<Object>) result;
         POJOMockInfo pmi = context.getMockInfo();
         int randomSize = Common.random(Math.max(1, pmi.getMin()),
               pmi.getMax());

         for (int i = 0; i < randomSize; i++) {
            MockContextHelper.enter();
            try {
               // if (elementType instanceof TypeVariable) {
               // @SuppressWarnings("unchecked")
               // Type toTypeReference = context
               // .findTypeVariableActurlType((TypeVariable<Class<?>>)
               // elementType);
               // collection.add(mockByType(toTypeReference));
               // } else
               collection.add(mockByType(context, elementType));
            } finally {
               MockContextHelper.leave();
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   private Type findElementType(Class<?> collectionType,
         ParameterizedType type) {

      if (Collection.class.isAssignableFrom(collectionType)) {
         @SuppressWarnings("rawtypes")
         TypeVariable tv = Collection.class.getTypeParameters()[0];
         Type t = TypeHelper.searchActualType(tv, type);
         if (t == null || t instanceof TypeVariable) {
            t = type.getActualTypeArguments()[0];
         }
         return t;
      } else
         return null;
   }

   private Type findMapType(Class<?> collectionType, ParameterizedType type,
         int index) {

      if (Map.class.isAssignableFrom(collectionType)) {
         @SuppressWarnings("rawtypes")
         TypeVariable tv = Map.class.getTypeParameters()[index];
         @SuppressWarnings("unchecked")
         Type t = TypeHelper.searchActualType(tv, type);
         if (t == null || t instanceof TypeVariable) {
            t = type.getActualTypeArguments()[0];
         }
         return t;
      } else
         return null;
   }

}
