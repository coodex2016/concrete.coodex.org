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

package org.coodex.concrete.support.websocket;

import org.coodex.concrete.common.Caller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;

public class CallerHackFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                HttpSession session = httpServletRequest.getSession(true);
                String xff = httpServletRequest.getHeader("X-Forwarded-For");
                session.setAttribute(CallerHackConfigurator.WEB_SOCKET_CALLER_INFO,
                        new HackedCaller(
                                xff == null ? httpServletRequest.getRemoteAddr() : xff.split(",")[0].trim(),
                                httpServletRequest.getHeader("User-Agent")));
            }
        } catch (Throwable th) {
            log.warn(th.getLocalizedMessage(), th);
        } finally {
            chain.doFilter(request, response);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CallerHackFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private static class HackedCaller implements Caller, Serializable {
        private final String ip;
        private final String userAgent;

        private HackedCaller(String ip, String userAgent) {
            this.ip = ip;
            this.userAgent = userAgent;
        }

        @Override
        public String getAddress() {
            return ip;
        }

        @Override
        public String getClientProvider() {
            return userAgent;
        }
    }

    @Override
    public void destroy() {

    }
}
