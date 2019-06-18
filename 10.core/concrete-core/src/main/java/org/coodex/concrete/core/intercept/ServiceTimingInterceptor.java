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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.ServiceTiming;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceTimingChecker;
import org.coodex.concrete.core.intercept.annotations.Local;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.intercept.annotations.TestContext;
import org.coodex.concrete.core.intercept.timecheckers.ByTimeRange;
import org.coodex.concrete.core.intercept.timecheckers.ByWorkDay;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by davidoff shen on 2016-11-02.
 */
@ServerSide
@TestContext
@Local
public class ServiceTimingInterceptor extends AbstractInterceptor {


    public static final String NAMESPACE_SERVICE_TIMING = "serviceTiming";
    private final static Logger log = LoggerFactory.getLogger(ServiceTimingInterceptor.class);
    private final static Map<String, String> DEFAULT_CHECKERS = new HashMap<String, String>();
    private static final ServiceTimingChecker ALL_ALLOWED_CHECKER = new ServiceTimingChecker() {
        @Override
        public boolean isAllowed() {
            return true;
        }
    };

    static {
        DEFAULT_CHECKERS.put("TIMERANGE", ByTimeRange.class.getCanonicalName());
        DEFAULT_CHECKERS.put("WORKDAY", ByWorkDay.class.getCanonicalName());
    }

    private static ServiceTimingChecker loadCheckerInstance(String label) {
//        Profile_Deprecated profile = Profile_Deprecated.getProfile("serviceTiming.properties");

        // 确定规则类型
        String type = Config.get(label + ".type", NAMESPACE_SERVICE_TIMING);
        String className = null;
        if (Common.isBlank(type)) {
            log.warn("ServiceTiming validation rule not defined. : {} ", label);
            return ALL_ALLOWED_CHECKER;
        }
        type = type.trim();
        className = DEFAULT_CHECKERS.get(type.toUpperCase());
        if (className == null) {
            className = type;
        }

        // 加载实例
        try {
            Class clz = Class.forName(className);
            Object o = clz.newInstance();
            for (Field f : ReflectHelper.getAllDeclaredFields(clz)) {
                f.setAccessible(true);
                if (!Modifier.isStatic(f.getModifiers()) // 非static
                        && !Modifier.isFinal(f.getModifiers()) // 非final
                        && String.class.equals(f.getType())) { // String类型
                    String s = Config.get(label + "." + f.getName(), NAMESPACE_SERVICE_TIMING);
                    if (s != null) {
                        f.set(o, s.trim());
                    }
                }
            }
            return (ServiceTimingChecker) o;
        } catch (Throwable t) {
            log.warn("error occurred in ServiceTimingChecker initialization. {} :{}",
                    t.getClass().getCanonicalName(), t.getLocalizedMessage());
        }

        return ALL_ALLOWED_CHECKER;
    }

    private static ServiceTimingChecker getValidator(ServiceTiming serviceTiming) {
        if (serviceTiming == null || serviceTiming.value().length == 0)
            return ALL_ALLOWED_CHECKER;
        else
            return new ServiceTimingCheckerChain(serviceTiming);
    }

    @Override
    protected boolean accept_(RuntimeContext context) {
        return isTimingLimitService(context);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.SERVICE_TIMING;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        IF.not(getValidator(context.getAnnotation(ServiceTiming.class)).isAllowed(),
                ErrorCodes.OUT_OF_SERVICE_TIME);
    }

    public boolean isTimingLimitService(RuntimeContext context) {
        return isServiceMethod(context)
                && (context.getAnnotation(ServiceTiming.class) != null);
    }

    private static class ServiceTimingCheckerChain implements ServiceTimingChecker {

        private List<ServiceTimingChecker> chain = new ArrayList<ServiceTimingChecker>();

        ServiceTimingCheckerChain(ServiceTiming serviceTiming) {
            Set<String> keys = new HashSet<String>();
            for (String s : serviceTiming.value()) {
                // 全天候
                if (Common.isBlank(s) || Common.isBlank(s.trim())) continue;
                s = s.trim();

                // 已包含
                if (keys.contains(s)) continue;

                keys.add(s);
                chain.add(loadCheckerInstance(s));
            }
        }

        @Override
        public boolean isAllowed() {
            for (ServiceTimingChecker checker : chain) {
                if (!checker.isAllowed())
                    return false;
            }
            return true;
        }
    }

}
