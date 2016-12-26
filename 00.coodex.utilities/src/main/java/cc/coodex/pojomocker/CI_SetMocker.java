/**
 * 
 */
package cc.coodex.pojomocker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author davidoff
 *
 */
public class CI_SetMocker extends CI_AbstractCollectionsMocker {

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.pojomock.refactoring.mockers.CI_AbstractCollectionsMocker#
    * createCollectionInstance(org.coodex.pojomock.refactoring.MockContext)
    */
   @SuppressWarnings("rawtypes")
   @Override
   protected Collection<?> createCollectionInstance(MockContext context) {
      return new HashSet();
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
      return clz == Set.class;
   }

}
