/**
 * 
 */
package org.coodex.pojomocker;

import org.coodex.util.Common;
import org.coodex.util.TypeHelper;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author davidoff
 *
 */
public class MockContext {

   public Map<Class<?>, Integer> getCreated() {
      return created;
   }

   public void addCreatedCount(Class<?> clz) {
      Integer i = created.get(clz);
      if (i == null) {
         i = 1;
      } else {
         i++;
      }
      created.put(clz, i);
   }

   private final Map<Class<?>, Integer> created = new HashMap<Class<?>, Integer>();
   private MockContext parent = null;
   private POJOMockInfo mockInfo = null;
   private Object instance = null;
   private Map<TypeVariantReplaceKey, Type> replaceMap = new HashMap<TypeVariantReplaceKey, Type>();
   private List<Type> contextClasses = new ArrayList<Type>();

   // private Type typeContext;
   private int arrayLevel = 0;

   MockContext(MockContext parent) {
      // TODO 根据上一个上下文构建一个新的上下文
      if (parent != null) {
         Common.copyMap(parent.created, created);
         mockInfo = parent.mockInfo;
         arrayLevel = parent.arrayLevel;
         instance = parent.instance;
         replaceMap.putAll(parent.replaceMap);
         contextClasses.addAll(parent.contextClasses);
         this.parent = parent;

      }
   }

   MockContext addContextType(Type contextClass) {
      this.contextClasses.add(contextClass);
      return this;
   }

   void addReplace(Type declared, int index, Type changeTo) {
      replaceMap.put(new TypeVariantReplaceKey(index, declared), changeTo);
   }

   public Object getInstance() {
      return instance;
   }

   private Type getReplacedType(TypeVariable<Class<?>> type) {
      Type[] types = type.getGenericDeclaration().getTypeParameters();
      int index = -1;
      for (int i = 0; i < types.length; i++) {
         if (type.equals(types[i])) {
            index = i;
            break;
         }
      }
      if (index >= 0) {
         return replaceMap.get(new TypeVariantReplaceKey(index, type.getGenericDeclaration()));
      } else
         return null;
   }

   public Type findTypeVariableActurlType(TypeVariable<Class<?>> type) {

      Type t = getReplacedType(type);
      if (t != null) {
         return t;
      }

      for (int i = contextClasses.size() - 1; i >= 0; i--) {
         t = TypeHelper.findActualClassFromInstanceClass(type,
               contextClasses.get(i));
         if (t != null)
            return t;
      }

      return null;
   }

   // public Class<?> getInstancedClass() {
   // return instance == null ? null : instance.getClass();
   // }

   // public void setInstance(Object instance) {
   // if (instance != null && !contextClasses.contains(instance.getClass())) {
   // contextClasses.add(instance.getClass());
   // }
   // this.instance = instance;
   // }

   // public Class<?>[] getContextClasses() {
   // Class<?>[] classes = contextClasses.toArray(new Class<?>[0]);
   // int size = classes.length, half = size / 2;
   // for (int i = 0; i < half; i++) {
   // int symmetric = size - 1 - i;
   // Class<?> $ = classes[i];
   // classes[i] = classes[symmetric];
   // classes[symmetric] = $;
   // }
   // return classes;
   // }

   public MockContext getParent() {
      return parent;
   }

   // public Type getTypeContext() {
   // return typeContext;
   // }
   //
   // public void setTypeContext(Type typeContext) {
   // this.typeContext = typeContext;
   // }

   public POJOMockInfo getMockInfo() {
      return mockInfo;
   }

   public void setMockInfo(POJOMockInfo mockInfo) {
      this.mockInfo = mockInfo;
   }

   public POJOMockerFactory getFactory() {
      return POJOMocker.getFactory(mockInfo.getFactoryClass());
   }

   public void arrayLevelAdd() {
      arrayLevel++;
   }

   public void arrayLevelReduce() {
      arrayLevel--;
   }

   public int getArrayLevel() {
      return arrayLevel;
   }

}
