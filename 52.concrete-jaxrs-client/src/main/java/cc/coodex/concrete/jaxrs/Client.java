package cc.coodex.concrete.jaxrs;

import cc.coodex.concrete.api.ConcreteService;
import cc.coodex.concrete.common.ConcreteToolkit;
import cc.coodex.concrete.common.ConcreteSPIFacade;
import cc.coodex.concrete.common.DefinitionContext;
import cc.coodex.concrete.jaxrs.client.ClientInstanceFactory;
import cc.coodex.concrete.jaxrs.client.Invoker;
import cc.coodex.concrete.jaxrs.client.InvokerFactory;
import cc.coodex.concrete.jaxrs.client.impl.JavaProxyClientInstanceFactory;
import cc.coodex.concrete.jaxrs.struct.Module;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.util.SPIFacade;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * java client for concrete jaxrs service
 * <p>
 * Created by davidoff shen on 2016-12-07.
 */
public final class Client {

    private static final SPIFacade<ClientInstanceFactory> BUILDER = new SPIFacade<ClientInstanceFactory>() {
        private ClientInstanceFactory defaultFactory = new JavaProxyClientInstanceFactory();

        @Override
        protected ClientInstanceFactory getDefaultProvider() {
            return defaultFactory;
        }
    };

    private static Map<String, ConcreteService> INSTANCE_CACHE =
            new HashMap<String, ConcreteService>();

    private static String getKey(Class<? extends ConcreteService> type, String domain) {
        return type.getName() + (domain == null ? "" : ("@" + domain));
    }

    /**
     * 获得实例
     *
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConcreteService> T getBean(Class<? extends T> type, String domain) {
        if (type == null) throw new NullPointerException("type MUST NOT NULL.");
        synchronized (INSTANCE_CACHE) {
            String key = getKey(type, domain);
            ConcreteService instance = INSTANCE_CACHE.get(key);
            if (instance == null) {
                instance = BUILDER.getInstance().create(type, domain);
                INSTANCE_CACHE.put(key, instance);
            }
            return (T) instance;
        }
    }

    public static <T extends ConcreteService> T getBean(Class<? extends T> type) {
        return getBean(type, null);
    }


    private static final SPIFacade<InvokerFactory> INVOKER_FACTORY_SPI_FACADE =
            new ConcreteSPIFacade<InvokerFactory>() {
            };

    protected static String getServiceRoot(String domain) {

        String s = domain == null ?
                ConcreteToolkit.getProfile().getString("concrete.serviceRoot", "").trim() :
                ConcreteToolkit.getProfile().getString("concrete." + domain + ".serviceRoot", domain);
        char[] buf = s.toCharArray();
        int len = buf.length;
        while (len > 0 && buf[len - 1] == '/') {
            len--;
        }
        return new String(buf, 0, len);
    }


    public static Invoker getInvoker(String domain) {

        domain = getServiceRoot(domain);

        for (InvokerFactory factory : INVOKER_FACTORY_SPI_FACADE.getAllInstances()) {
            if (factory.accept(domain)) {
                return factory.getInvoker(domain);
            }
        }
        throw new RuntimeException("unable found "
                + InvokerFactory.class.getName() + " service for [" + domain + "]");
    }


    // ------------
    public static final Unit getUnitFromContext(DefinitionContext context, MethodInvocation invocation) {
        Module module = JaxRSHelper.getModule(context.getDeclaringClass());
        Method method = context.getDeclaringMethod();
        int count = invocation.getArguments() == null ? 0 : invocation.getArguments().length;
        for (Unit unit : module.getUnits()) {
            if (method.getName().equals(unit.getMethod().getName())
                    && count == unit.getParameters().length) {
                return unit;
            }
        }
        return null;
    }

}
