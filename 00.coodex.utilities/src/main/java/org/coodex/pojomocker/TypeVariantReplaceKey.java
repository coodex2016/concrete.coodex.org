/**
 * 
 */
package org.coodex.pojomocker;

import java.lang.reflect.Type;

/**
 * @author davidoff
 *
 */
final class TypeVariantReplaceKey {
   private int index;
   private Type declared;
   /**
    * @param index
    * @param declared
    */
   public TypeVariantReplaceKey(int index, Type declared) {
      super();
      this.index = index;
      this.declared = declared;
   }
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((declared == null) ? 0 : declared.hashCode());
      result = prime * result + index;
      return result;
   }
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      TypeVariantReplaceKey other = (TypeVariantReplaceKey) obj;
      if (declared == null) {
         if (other.declared != null)
            return false;
      } else if (!declared.equals(other.declared))
         return false;
       return index == other.index;
   }
   
   
   
}
