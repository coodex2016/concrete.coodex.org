/**
 * 
 */
package cc.coodex.pojomocker;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author davidoff
 *
 */
public abstract class POJOMockerFactory {

   private final Collection<AbstractClassInstanceMocker> mockerProducts = new ArrayList<AbstractClassInstanceMocker>();

   private static final Collection<AbstractClassInstanceMocker> mockers = new ArrayList<AbstractClassInstanceMocker>();

   private static final AbstractClassInstanceMocker defaultClassMocker = new CI_DefaultMocker();

   private final AbstractClassInstanceMocker findMockerFromList(Class<?> clz,
         Collection<AbstractClassInstanceMocker> list) {
      for (AbstractClassInstanceMocker mocker : list) {
         if (mocker.access(clz))
            return mocker;
      }
      return null;
   }

   /**
    * 注册一个工厂特定的ClassMocker
    * 
    * @param mocker
    */
   protected final void registMocker(AbstractClassInstanceMocker mocker) {
      if (mocker != null && !mockerProducts.contains(mocker))
         mockerProducts.add(mocker);
   }

   /**
    * 注册全局的ClassMocker
    * 
    * @param mocker
    */
   static void registGlobalMocker(AbstractClassInstanceMocker mocker) {
      if (mocker != null && !mockers.contains(mocker))
         mockers.add(mocker);
   }

   public final AbstractClassInstanceMocker getClassInstanceMocker(Class<?> clz) {
      AbstractClassInstanceMocker mocker = findMockerFromList(clz, mockers);
      if (mocker == null)
         mocker = findMockerFromList(clz, mockerProducts);
      if (mocker == null)
         mocker = defaultClassMocker;
      return mocker;
   }

}
