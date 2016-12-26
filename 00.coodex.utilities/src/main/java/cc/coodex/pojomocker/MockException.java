/**
 * 
 */
package cc.coodex.pojomocker;

/**
 * @author davidoff
 *
 */
public abstract class MockException extends Exception {
   private static final long serialVersionUID = -3884473801380757433L;

   protected MockException() {
   }

   protected MockException(String msg) {
      super(msg);
   }

   protected MockException(Throwable th) {
      super(th);
   }

   protected MockException(String msg, Throwable th) {
      super(msg, th);
   }
}