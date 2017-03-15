/**
 * 
 */
package org.coodex.pojomocker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author davidoff
 *
 */
public class CI_MapMocker extends AbstractUnmockFieldMocker {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.AbstractClassInstanceMocker#access(java.lang.Class)
    */
   @Override
   protected boolean access(Class<?> clz) {
      return Map.class == clz;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.AbstractClassInstanceMocker#newInstance(java.lang.
    * Class, org.coodex.pojomock.MockContext)
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   protected <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException, UnableMockException {
      return (T) new HashMap();
   }

}
