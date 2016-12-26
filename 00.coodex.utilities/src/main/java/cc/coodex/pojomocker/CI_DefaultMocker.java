/**
 * 
 */
package cc.coodex.pojomocker;

/**
 * @author davidoff
 *
 */
public class CI_DefaultMocker extends AbstractClassInstanceMocker {

   @Override
   protected boolean access(Class<?> clz) {
      return true;
   }

   @Override
   protected boolean needCreate(int created, MockContext context) {
      return created <= context.getMockInfo().getMaxRecycledCount();
   }

   @Override
   protected boolean needMockFields(Class<?> clz) {
      String packageName = clz.getPackage().getName();
      return !packageName.startsWith("java");
   }

   @Override
   protected <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException {
      try {

         return clz.newInstance();
      } catch (InstantiationException e) {
         throw new UnsupportedTypeException(clz);
      }
   }

}
