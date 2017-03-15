/**
 * 
 */
package org.coodex.pojomocker;

import java.util.Collection;

/**
 * @author davidoff
 *
 */
public abstract class CI_AbstractCollectionsMocker extends
      AbstractUnmockFieldMocker {

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.coodex.pojomock.refactoring.AbstractClassInstanceMocker#newInstance
    * (java.lang.Class, org.coodex.pojomock.refactoring.MockContext)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected final <T> T newInstance(Class<T> clz, MockContext context)
         throws UnsupportedTypeException, IllegalAccessException,
         IllegalArgumentException {

      return (T) createCollectionInstance(context);
   }

   @Override
   public POJOMockInfo getDefaultMockInfo() {

      POJOMockInfo pmi = super.getDefaultMockInfo();
      pmi.setMax(10);
      pmi.setMin(1);
      return pmi;
   }

   protected abstract Collection<?> createCollectionInstance(MockContext context);

}
