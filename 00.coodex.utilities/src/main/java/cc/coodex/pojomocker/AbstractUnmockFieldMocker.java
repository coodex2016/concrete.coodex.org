/**
 * 
 */
package cc.coodex.pojomocker;


/**
 * @author davidoff
 *
 */
public abstract class AbstractUnmockFieldMocker extends
      AbstractClassInstanceMocker {


   /* (non-Javadoc)
    * @see org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#needCreate(int, org.coodex.pojomock.refactoring.MockContext)
    */
   @Override
   protected final boolean needCreate(int created, MockContext context) {
      return true;
   }

   /* (non-Javadoc)
    * @see org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#needMockFields(java.lang.Class)
    */
   @Override
   protected final boolean needMockFields(Class<?> clz) {
      return false;
   }


}
