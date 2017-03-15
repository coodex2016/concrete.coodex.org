/**
 * 
 */
package org.coodex.pojomocker;

import org.coodex.util.Common;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * 泛型数组模拟器
 * 
 * @author davidoff
 *
 */
public class GenericArrayTypeMocker extends
      AbstractTypeBasedMocker<GenericArrayType> {

   @Override
   protected Class<?> getTypeClass() {
      return GenericArrayType.class;
   }

   @Override
   protected Object $mock(GenericArrayType type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException, UnableMockException {
      POJOMockInfo pmi = context.getMockInfo();
      int level = context.getArrayLevel();
      int[] arraySize = pmi.getArraySize();
      int size = level < arraySize.length ? arraySize[level] : Common.random(
            pmi.getMin(), pmi.getMax());
      if (size < 0) {
         size = Common.random(pmi.getMin(), pmi.getMax());
      }

      Type componentType = type.getGenericComponentType();
      context.arrayLevelAdd();
      try {
         Object array = null;
         for (int i = 0; i < size; i++) {
            MockContextHelper.enter();
            try {
               Object element = mockByType(componentType);
               if (array == null)
                  array = Array.newInstance(element.getClass(), size);

               Array.set(array, i, element);
            } finally {
               MockContextHelper.leave();
            }
         }
         return array;
      } finally {
         context.arrayLevelReduce();
      }
   }

}
