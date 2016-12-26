/**
 * 
 */
package cc.coodex.pojomocker;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

/**
 * @author davidoff
 *
 */
public class TypeVariableNotInClassException extends
      NoActualClassFoundException {

   private static final long serialVersionUID = -569028824595244181L;

   public TypeVariableNotInClassException(TypeVariable<?> causedType) {
      super(causedType, null);
   }
   
   @Override
   protected String getCustomMessage() {
      return "TypeVariable " + getTypeVariable() + " is declared in a ["
            + getDeclarationName() + "]";
   }

   private String getDeclarationName() {
      GenericDeclaration gd = getTypeVariable().getGenericDeclaration();
      if (gd instanceof Method)
         return "Method";
      else if (gd instanceof Constructor)
         return "Construtor";
      else if (gd instanceof Class) {
         return "Class ?? MISCALCULATION !!!";
      } else
         return "Unknown";

   }

   @SuppressWarnings("unchecked")
   private TypeVariable<GenericDeclaration> getTypeVariable() {
      return (TypeVariable<GenericDeclaration>) getCausedType();
   }

}
