/**
 * 
 */
package org.coodex.pojomocker;

import java.lang.reflect.Type;

/**
 * @author davidoff
 *
 */
public class NoActualClassFoundException extends UnsupportedTypeException {

   /**
    * 
    */
   private static final long serialVersionUID = -8858497101761412839L;
   private final Class<?> instancedClass;

   /**
    * @param causedType
    */
   public NoActualClassFoundException(Type causedType, Class<?> instancedClass) {
      super(causedType);
      this.instancedClass = instancedClass;
   }

   @Override
   protected String getCustomMessage() {
      return "No actual class found: " + this.getCausedType().toString()
            + " in " + instancedClass;
   }

}
