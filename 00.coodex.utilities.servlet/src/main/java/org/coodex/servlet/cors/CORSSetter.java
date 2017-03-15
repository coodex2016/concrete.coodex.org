/**
 * 
 */
package org.coodex.servlet.cors;

import java.util.StringTokenizer;

/**
 * @author davidoff
 *
 */
public class CORSSetter {

   public static void set(CORSSetting setting, HeaderSetter setter,
         String origin) {
      Boolean allowCredentials = setting.allowCredentials();
      Long maxAge = setting.maxAge();
      String allowHeaders = setting.allowHeaders();
      String allowMethod = setting.allowMethod();
      String allowOrigin = setting.allowOrigin();
      String exposeHeaders = setting.exposeHeaders();

      if (allowCredentials != null)
         setter.set(CORSSetting.ALLOW_CREDENTIALS,
               String.valueOf(allowCredentials.booleanValue()));

      if (maxAge != null)
         setter.set(CORSSetting.MAX_AGE, String.valueOf(maxAge.longValue()));

      if (allowHeaders != null)
         setter.set(CORSSetting.ALLOW_HEADERS, allowHeaders);

      if (allowOrigin != null) {
         StringTokenizer st = new StringTokenizer(allowOrigin, ", ", false);
         while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("*") || s.equalsIgnoreCase(origin)) {
               setter.set(CORSSetting.ALLOW_ORIGIN, origin);
               break;
            }
         }            
      }

      if (allowMethod != null)
         setter.set(CORSSetting.ALLOW_METHOD, allowMethod);

      if (exposeHeaders != null)
         setter.set(CORSSetting.EXPOSE_HEADERS, exposeHeaders);

   }

}
