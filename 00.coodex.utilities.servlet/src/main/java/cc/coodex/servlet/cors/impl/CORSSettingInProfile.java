/**
 * 
 */
package cc.coodex.servlet.cors.impl;

import cc.coodex.servlet.cors.CORSSetting;
import cc.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author davidoff
 *
 */
public class CORSSettingInProfile implements CORSSetting {

   private static Logger log = LoggerFactory
         .getLogger(CORSSettingInProfile.class);

   private final Profile profile;

   public CORSSettingInProfile() {
      this("cors_settings.properties");
   }

   public CORSSettingInProfile(String profileName) {
      profile = Profile.getProfile(profileName);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#allowOrigin()
    */
   @Override
   public String allowOrigin() {
      return profile.getString("allowOrigin");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#exposeHeaders()
    */
   @Override
   public String exposeHeaders() {
      return profile.getString("exposeHeaders");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#allowMethod()
    */
   @Override
   public String allowMethod() {
      return profile.getString("allowMethod");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#allowHeaders()
    */
   @Override
   public String allowHeaders() {
      return profile.getString("allowHeaders");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#maxAge()
    */
   @Override
   public Long maxAge() {
      String maxAge = profile.getString("maxAge");
      Long lMaxAge = null;
      try {
         if (maxAge != null)
            lMaxAge = Long.valueOf(maxAge);
      } catch (Throwable th) {
         log.info("cannot load maxAge, type need: long. {} in {}",
               th.getLocalizedMessage(), profile.getLocation(), th);
      }
      return lMaxAge;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.coodex.concrete.sp.cors.CORSSetting#allowCredentials()
    */
   @Override
   public Boolean allowCredentials() {
      String allowCredentials = profile.getString("allowCredentials");
      Boolean blAC = null;
      try {
         if (allowCredentials != null)
            blAC = Boolean.valueOf(allowCredentials);

      } catch (Throwable th) {
         log.info("cannot load allowCredentials, type need: boolean. {} in {}",
               th.getLocalizedMessage(), profile.getLocation(), th);
      }

      return blAC;
   }

}
