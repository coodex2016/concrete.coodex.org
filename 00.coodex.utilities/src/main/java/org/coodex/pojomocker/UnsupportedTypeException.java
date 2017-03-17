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

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author davidoff
 *
 */
public class UnsupportedTypeException extends MockException {

   private static final long serialVersionUID = 3609086274197974556L;
   private final Type causedType;

   private String msg = null;

   public Type getCausedType() {
      return causedType;
   }

   public UnsupportedTypeException(Type causedType) {
      this.causedType = causedType;
   }

   @Override
   public String getLocalizedMessage() {
      return "Unsupported Type: " + causedType.toString() + "\n" + getMsg();
   }

   @Override
   public String getMessage() {
      return getLocalizedMessage();
   }

   protected String getCustomMessage() {
      return null;
   }

   private synchronized String getMsg() {
      if (msg == null) {
         StringBuffer buf = new StringBuffer();
         String customMessage = getCustomMessage();
         buf.append(customMessage != null ? customMessage : causedType);
         buf.append(getTypeInfo(causedType, "  "));
         msg = buf.toString();
      }
      return msg;
   }

   public static String getTypeInfo(Type type) {
      return getTypeInfo(type, null);
   }

   private static String getTypeInfo(Type type, String indent) {
      boolean showType = indent == null;
      indent = showType ? "" : indent;
      String indentStep = "    ";
      StringBuffer buf = new StringBuffer();

      if (showType)
         buf.append("Type: ").append(type);

      // type: Class
      boolean flag = type instanceof Class;
      if (flag) {
         buf.append("\n  ").append(indent).append("isClass: ").append(flag);
         Class<?> c = (Class<?>) type;
         if (c.isInterface())
            buf.append("\n    ").append(indent).append("isInterface: ")
                  .append(true);

         if ((c.getModifiers() & Modifier.ABSTRACT) != 0)
            buf.append("\n    ").append(indent).append("isAbstract: ")
                  .append(true);

         if (c.isAnnotation())
            buf.append("\n    ").append(indent).append("isAnnotation: ")
                  .append(true);

         if (c.isArray())
            buf.append("\n    ").append(indent).append("isArray: ")
                  .append(true);

      }

      // type: ParameterizedType
      flag = type instanceof ParameterizedType;
      if (flag) {
         buf.append("\n  ").append(indent).append("isParameterizedType: ")
               .append(flag);
         ParameterizedType pt = (ParameterizedType) type;
         Type ownerType = pt.getOwnerType();
         Type rawType = pt.getRawType();
         Type[] actualParams = pt.getActualTypeArguments();
         buf.append("\n    ").append(indent).append("OwnerType: ")
               .append(ownerType);
         if (ownerType != null)
            buf.append(getTypeInfo(ownerType, indent + indentStep));
         buf.append("\n    ").append(indent).append("RawType: ")
               .append(rawType);
         if (rawType != null)
            buf.append(getTypeInfo(rawType, indent + indentStep));
         if (actualParams != null) {
            for (int i = 0; i < actualParams.length; i++) {
               buf.append("\n    ").append(indent)
                     .append("getActualTypeArguments[" + i + "]: ")
                     .append(actualParams[i]);
               if (actualParams[i] != null)
                  buf.append(getTypeInfo(actualParams[i], indent + indentStep));
            }
         }
      }

      // type: TypeVariable
      flag = type instanceof TypeVariable;
      if (flag) {
         buf.append("\n  ").append(indent).append("isTypeVariable: ")
               .append(flag);
         TypeVariable<?> tv = (TypeVariable<?>) type;
         Type[] bounds = tv.getBounds();
         Object c = tv.getGenericDeclaration();
         buf.append("\n    ").append(indent).append("GenericDeclaration: ")
               .append(c);
         if (bounds != null) {
            for (int i = 0; i < bounds.length; i++) {
               buf.append("\n    ").append(indent)
                     .append("bounds[" + i + "]: ").append(bounds[i]);
               if (bounds[i] != null)
                  buf.append(getTypeInfo(bounds[i], indent + indentStep));
            }
         }
      }
      return buf.toString();
   }
}
