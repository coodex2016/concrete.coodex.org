/**
 * 
 */
package cc.coodex.servlet.cors;

/**
 * 基于www.w3.org/TR/cors设计的CORS参数设定，定义了5.1至5.6的6个响应头。版本：20140116
 * 
 * @author davidoff
 *
 */
public interface CORSSetting {

   public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
   public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
   public static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";
   public static final String MAX_AGE = "Access-Control-Max-Age";
   public static final String ALLOW_METHOD = "Access-Control-Allow-Method";
   public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";

   /**
    * 空格或逗号分隔，为null表示不需要设置
    * 
    * @return
    */
   String allowOrigin();

   /**
    * 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
    * 
    * @return
    */
   String exposeHeaders();

   /**
    * 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
    * 
    * @return
    */
   String allowMethod();

   /**
    * 依RFC 2616规范，使用逗号分隔，为null表示不需要设置
    * 
    * @return
    */
   String allowHeaders();

   /**
    * 为null表示不需要设置
    * 
    * @return
    */
   Long maxAge();

   /**
    * 为null表示不需要设置
    * 
    * @return
    */
   Boolean allowCredentials();

}
