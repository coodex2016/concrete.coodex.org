/**
 * 
 */
package cc.coodex.pojomocker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author davidoff
 *
 */
public class CI_ListMocker extends CI_AbstractCollectionsMocker {

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.pojomock.refactoring.mockers.CI_AbstractCollectionsMocker#
    * createCollectionInstance(org.coodex.pojomock.refactoring.MockContext)
    */
   @SuppressWarnings("rawtypes")
   @Override
   protected Collection<?> createCollectionInstance(MockContext context) {
      return new ArrayList();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#access(java
    * .lang.Class)
    */
   @Override
   protected boolean access(Class<?> clz) {
      return clz == List.class || clz == Collection.class;
   }

}
