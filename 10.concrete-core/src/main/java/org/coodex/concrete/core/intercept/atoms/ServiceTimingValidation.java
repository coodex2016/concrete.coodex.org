package org.coodex.concrete.core.intercept.atoms;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.NotService;
import org.coodex.concrete.api.ServiceTiming;
import org.coodex.concrete.api.ServiceTimingChecker;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.timecheckers.ByTimeRange;
import org.coodex.concrete.core.intercept.timecheckers.ByWorkDay;
import org.coodex.util.Common;
import org.coodex.util.Profile;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 服务提供时间判定
 * <p>
 * 在非 {@link NotService} 接口上定义 {@link ServiceTiming}. <br/>
 * 在serviceTiming.properties里定义验证规则，规则模版如下:<pre>
 *     <i>ruleName</i>.type = ...<br/>
 *     <i>ruleName</i>.<i>properties</i> = <i>some value</i>
 *     ...
 * <br/>type可以为以下值:
 *     timerange: 根据时间提供服务，参见{@link ByTimeRange}
 *     workday: 根据日期提供服务，参见{@link ByWorkDay}
 *     自己实现{@link ServiceTimingChecker}的类名
 * <br/>自定义class：
 *     1、属性名与class中的非static非final的String类型的域相对应，自动装载
 *     2、自定义的class需要能够无参数构造实例
 * </pre>
 * <p>
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
public class ServiceTimingValidation {

    private final static Logger log = LoggerFactory.getLogger(ServiceTimingValidation.class);


    private final static Map<String, String> DEFAULT_CHECKERS = new HashMap<String, String>();

    static {
        DEFAULT_CHECKERS.put("TIMERANGE", ByTimeRange.class.getCanonicalName());
        DEFAULT_CHECKERS.put("WORKDAY", ByWorkDay.class.getCanonicalName());
    }

    public static boolean isTimingLimitService(RuntimeContext context) {
        return context.getDeclaringMethod().getAnnotation(NotService.class) == null
                && (context.getDeclaringMethod().getAnnotation(ServiceTiming.class) != null
                || context.getDeclaringClass().getAnnotation(ServiceTiming.class) != null);
    }


    private static ServiceTimingChecker loadCheckerInstance(String label) {
        Profile profile = Profile.getProfile("serviceTiming.properties");

        // 确定规则类型
        String type = profile.getString(label + ".type");
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
                    String s = profile.getString(label + "." + f.getName());
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

    private static class ServiceTimingCheckerChain implements ServiceTimingChecker {

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

        private List<ServiceTimingChecker> chain = new ArrayList<ServiceTimingChecker>();

        @Override
        public boolean isAllowed() {
            for (ServiceTimingChecker checker : chain) {
                if (!checker.isAllowed())
                    return false;
            }
            return true;
        }
    }

    private static final ServiceTimingChecker ALL_ALLOWED_CHECKER = new ServiceTimingChecker() {
        @Override
        public boolean isAllowed() {
            return true;
        }
    };


    private static ServiceTimingChecker getValidator(ServiceTiming serviceTiming) {
        if (serviceTiming == null || serviceTiming.value().length == 0)
            return ALL_ALLOWED_CHECKER;
        else
            return new ServiceTimingCheckerChain(serviceTiming);
    }

    public static void before(RuntimeContext context, MethodInvocation joinPoint) {
//        ServiceTiming serviceTiming = joinPoint.getMethod().getAnnotation(ServiceTiming.class);
//        if (serviceTiming == null)
//            serviceTiming = context.getDeclaringMethod().getAnnotation(ServiceTiming.class);
//        if (serviceTiming == null)
//            serviceTiming = context.getDeclaringClass().getAnnotation(ServiceTiming.class);
        Assert.not(getValidator(context.getDeclaringAnnotation(ServiceTiming.class)).isAllowed(),
                ErrorCodes.OUT_OF_SERVICE_TIME);
    }
}
