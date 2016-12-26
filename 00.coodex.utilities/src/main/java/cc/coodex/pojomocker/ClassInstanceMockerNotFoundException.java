/**
 * 
 */
package cc.coodex.pojomocker;

import java.lang.reflect.Type;

/**
 * @author davidoff
 *
 */
public class ClassInstanceMockerNotFoundException extends
      UnsupportedTypeException {

   private static final long serialVersionUID = -1677907678558297900L;

   public ClassInstanceMockerNotFoundException(Type causedType) {
      super(causedType);
   }

   @Override
   protected String getCustomMessage() {
      return "ClassInstanceMocker for [" + getCausedType().toString()
            + "] NOT found.";
   }

}
