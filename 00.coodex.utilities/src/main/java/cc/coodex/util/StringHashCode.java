/**
 * 
 */
package cc.coodex.util;

/**
 * @author davidoff
 *
 */
public class StringHashCode {

   public static int BKDRHash(String str) {
      if (str == null)
         return 0;
      int seed = 131;
      int hash = 0;
      byte[] buf = str.getBytes();
      for (int i = 0; i < buf.length; i++) {
         hash = hash * seed + (int) buf[i];
      }
      return hash & 0x7FFFFFFF;
   }

}
