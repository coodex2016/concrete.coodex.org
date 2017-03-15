/**
 * 
 */
package org.coodex.pojomocker;


/**
 * Class类型模拟器
 * 
 * @author davidoff
 *
 */
public class ClassTypeMocker extends AbstractTypeBasedMocker<Class<?>> {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractTypeBasedMocker#getTypeClass()
    */
   @Override
   protected Class<?> getTypeClass() {
      return Class.class;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractTypeBasedMocker#$mock(java.lang
    * .reflect.Type, org.coodex.pojomock.refactoring.MockContext)
    */
   @Override
   protected Object $mock(Class<?> type, MockContext context)
         throws IllegalAccessException, IllegalArgumentException,
         UnsupportedTypeException, UnableMockException {

      return context.getFactory().getClassInstanceMocker(type)
            .mockInstance(type, context, null);
   }

}
