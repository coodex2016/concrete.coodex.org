/**
 * 
 */
package org.coodex.servlet.cors;

import org.coodex.servlet.cors.impl.CORSSettingInProfile;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * 
 * @author davidoff
 *
 */
public class CorsFilter implements Filter {

   private final CORSSetting corsSetting = new CORSSettingInProfile();

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
//   @Override
   public void init(FilterConfig filterConfig) throws ServletException {

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    * javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
//   @Override
   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain chain) throws IOException, ServletException {
      doFilter((HttpServletRequest) request, (HttpServletResponse) response,
            chain);

   }

   private void doFilter(HttpServletRequest request,
         final HttpServletResponse response, FilterChain chain)
         throws IOException, ServletException {

      CORSSetter.set(corsSetting, new HeaderSetter() {

//         @Override
         public void set(String header, String value) {
            response.setHeader(header, value);
         }
      }, request.getHeader("Origin"));

      chain.doFilter(request, response);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#destroy()
    */
//   @Override
   public void destroy() {
   }

}
