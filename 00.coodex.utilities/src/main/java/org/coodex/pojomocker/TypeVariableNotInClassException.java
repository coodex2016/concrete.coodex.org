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

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

/**
 * @author davidoff
 *
 */
public class TypeVariableNotInClassException extends
      NoActualClassFoundException {

   private static final long serialVersionUID = -569028824595244181L;

   public TypeVariableNotInClassException(TypeVariable<?> causedType) {
      super(causedType, null);
   }
   
   @Override
   protected String getCustomMessage() {
      return "TypeVariable " + getTypeVariable() + " is declared in a ["
            + getDeclarationName() + "]";
   }

   private String getDeclarationName() {
      GenericDeclaration gd = getTypeVariable().getGenericDeclaration();
      if (gd instanceof Method)
         return "Method";
      else if (gd instanceof Constructor)
         return "Construtor";
      else if (gd instanceof Class) {
         return "Class ?? MISCALCULATION !!!";
      } else
         return "Unknown";

   }

   @SuppressWarnings("unchecked")
   private TypeVariable<GenericDeclaration> getTypeVariable() {
      return (TypeVariable<GenericDeclaration>) getCausedType();
   }

}
