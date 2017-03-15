/**
 * 
 */
package org.coodex.pojomocker;

import org.coodex.util.Common;

import java.lang.reflect.Array;

/**
 * @author davidoff
 *
 */
public class CI_ArrayMocker extends AbstractUnmockFieldMocker {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#access(java
    * .lang.Class)
    */
   @Override
   protected boolean access(Class<?> clz) {
      return clz.isArray();
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
   protected <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {

      return (T) mockArray(clz, context, 0);
   }

   private Object mockArray(Class<?> clz, MockContext context, int level)
         throws IllegalAccessException,
         IllegalArgumentException, UnableMockException, UnsupportedTypeException {
      POJOMockInfo pmi = context.getMockInfo();

      int[] arraySize = pmi.getArraySize();
      int randomSize = Common.random(Math.max(1, pmi.getMin()), pmi.getMax());
      int size = level < arraySize.length ? arraySize[level] : randomSize;
      if (size < 0) {
         size = randomSize;
      }

      // 泛型数组单独由GenericArrayTypeMocker模拟
      Class<?> componentType = clz.getComponentType();
      Object array = Array.newInstance(componentType, size);

      for (int i = 0; i < size; i++) {
         if (componentType.isArray())
            Array.set(array, i, mockArray(componentType, context, level + 1));
         else
            Array.set(array, i,
                  context.getFactory().getClassInstanceMocker(componentType)
                        .mockInstance(componentType, context, null));
      }
      return array;

   }
}
