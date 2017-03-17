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

import java.lang.reflect.TypeVariable;

/**
 * 变量泛型模拟器
 * 
 * @author davidoff
 *
 */
public class TypeVariableMocker extends
      AbstractTypeBasedMocker<TypeVariable<?>> {

   @Override
   protected Class<?> getTypeClass() {
      return TypeVariable.class;
   }

   // private static int findTypeParameterIndex(TypeVariable<?> type)
   // throws TypeVariableNotInClassException {
   // TypeVariable<?>[] parameters = type.getGenericDeclaration()
   // .getTypeParameters();
   // for (int i = 0; i < parameters.length; i++) {
   // if (type == parameters[i])
   // return i;
   // }
   // throw new TypeVariableNotInClassException(type);
   // }

   @SuppressWarnings("unchecked")
   @Override
   protected Object $mock(TypeVariable<?> type, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {

      return mockByType(assertNull(
            context.findTypeVariableActurlType((TypeVariable<Class<?>>) type),
            type));
   }

   // @SuppressWarnings("unchecked")
   // private Type findType(TypeVariable<?> type, MockContext context) {
   // Class<?>[] contextClasses = context.getContextClasses();
   // for (Class<?> $ : contextClasses) {
   // Type toTypeReference = TypeHelper.findActualClassFromInstanceClass(
   // (TypeVariable<Class<?>>) type, $);
   // if (toTypeReference != null)
   // return toTypeReference;
   // }
   // return null;
   //
   // // Class<?> declaringClass = null;
   // // // int index = findTypeParameterIndex(type);
   // // GenericDeclaration gd = type.getGenericDeclaration();
   // // if (gd instanceof Class) {
   // // declaringClass = (Class<?>) gd;
   // // }
   //
   // // assertNull(declaringClass, type);
   //
   // // Type typeContext = context.getTypeContext();
   // //
   // // Type toMock = null;
   // // MockContext parent = context.getParent();
   // // while (parent != null) {
   // // System.out.println(typeContext);
   // // if (typeContext instanceof ParameterizedType) {
   // //
   // // ParameterizedType pt = (ParameterizedType) typeContext;
   // // if (pt.getRawType() == declaringClass) {
   // //
   // // Type[] acturlTypeArguments = pt.getActualTypeArguments();
   // // if (acturlTypeArguments == null
   // // || acturlTypeArguments.length <= index)
   // // throw new UnsupportedTypeException(type);
   // //
   // // toMock = acturlTypeArguments[index];
   // // break;
   // // }
   // // } else {
   // // typeContext = parent.getTypeContext();
   // // }
   // // parent = parent.getParent();
   // // }
   // //
   // // return toMock;
   // }

   private <T> T assertNull(T o, TypeVariable<?> type)
         throws UnsupportedTypeException {
      if (o == null)
         throw new UnsupportedTypeException(type);
      else
         return o;
   }

}
