/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.servlet.cors;

import org.coodex.servlet.cors.impl.CORSSettingInConfiguration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author davidoff
 */
public class CorsFilter implements Filter {

    private final CORSSetting corsSetting;// = new CORSSettingInProfile();

    public CorsFilter() {
        this(new CORSSettingInConfiguration());
    }


    public CorsFilter(CORSSetting corsSetting) {
        this.corsSetting = corsSetting;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
//   @Override
    public void init(FilterConfig filterConfig) {

    }

    protected CORSSetting getCorsSetting() {
        return corsSetting;
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

        //         @Override
        CORSSetter.set(getCorsSetting(), response::setHeader, request.getHeader("Origin"));

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
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
