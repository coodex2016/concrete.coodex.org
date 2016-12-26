package cc.coodex.concrete.jaxrs.client.impl;

import cc.coodex.concrete.api.ConcreteService;
import cc.coodex.concrete.jaxrs.Client;
import cc.coodex.concrete.jaxrs.client.ClientInstanceFactory;
import cc.coodex.concrete.jaxrs.struct.Module;
import cc.coodex.concrete.jaxrs.struct.Unit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JavaProxyClientInstanceFactory implements ClientInstanceFactory {


    @Override
    @SuppressWarnings("unchecked")
    public <T extends ConcreteService> T create(final Class<? extends T> type, final String domain) {
        try {

            InvocationHandler handler = new InvocationHandler() {
                private Module module = new Module(type);

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getDeclaringClass() == Object.class)
                        return method.invoke(this, args);
                    else {
                        int count = args == null ? 0 : args.length;
                        for (Unit unit : module.getUnits()) {
                            if (method.getName().equals(unit.getMethod().getName())
                                    && count == unit.getParameters().length) {

                                return Client.getInvoker(domain).invoke(unit, args);
                            }
                        }
                        throw new RuntimeException("method not found in [" + type.getName() + "]: [" + method.getName() + "] with " + count + " parameter(s).");
                    }
                }
            };

            return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{type}, handler);
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
    }
}
