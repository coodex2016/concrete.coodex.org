/**
 * 
 */
package org.coodex.pojomocker;

import java.lang.reflect.Type;

/**
 * 基于Type的mocker，用来实现Class/TypeVariant/ParameterizedType各自的模拟
 * 
 * @author davidoff
 *
 */
public abstract class AbstractTypeBasedMocker<T extends Type> {

   protected abstract Class<?> getTypeClass();

   final protected static Object mockByType(Type type)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      return POJOMocker.$mock(type);
   }

   @SuppressWarnings("unchecked")
   final Object mock(Type t, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      if (getTypeClass().isAssignableFrom(t.getClass()))
         return $mock((T) t, context);
      else
         throw new UnsupportedTypeException(t);
   }

   protected abstract Object $mock(T type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException,UnableMockException;

}
