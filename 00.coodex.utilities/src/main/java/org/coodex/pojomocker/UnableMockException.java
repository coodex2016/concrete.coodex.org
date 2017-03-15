/**
 * 
 */
package org.coodex.pojomocker;

/**
 * @author davidoff
 *
 */
public class UnableMockException extends MockException {

   private static final long serialVersionUID = 1813632640475854401L;

   public UnableMockException(Throwable th) {
      super(th);
   }

   public UnableMockException(String msg, Throwable th) {
      super(msg, th);
   }

   public UnableMockException(String msg) {
      super(msg);
   }

}
