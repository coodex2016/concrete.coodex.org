package cc.coodex.concrete.jaxrs.client;

import cc.coodex.concrete.common.ConcreteSPIFacade;
import cc.coodex.concrete.jaxrs.JaxRSHelper;
import cc.coodex.concrete.jaxrs.struct.Param;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.pojomocker.POJOMocker;
import cc.coodex.util.Common;
import cc.coodex.util.SPIFacade;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import static cc.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public abstract class AbstractRemoteInvoker extends AbstractInvoker {

    private static final SPIFacade<JSONSerializer> JSON_SERIALIZER_FACTORY = new ConcreteSPIFacade<JSONSerializer>() {
    };

    public static JSONSerializer getJSONSerializer() {
        return JSON_SERIALIZER_FACTORY.getInstance();
    }


    protected final String domain;

    public AbstractRemoteInvoker(String domain) {
        this.domain = domain;
    }

    @Override
    protected MethodInvocation getInvocation(final Unit unit, final Object[] args, final Object instance) {
        return new ClientMethodInvocation(instance, unit, args) {
            @Override
            public Object proceed() throws Throwable {
                if (FRONTEND_DEV_MODE) {
                    return POJOMocker.mock(unit.getGenericReturnType(), unit.getDeclaringModule().getInterfaceClass());
                } else {

                    String path = domain + unit.getDeclaringModule().getName();
                    StringTokenizer stringTokenizer = new StringTokenizer(unit.getName(), "/");
                    StringBuilder builder = new StringBuilder();

                    while (stringTokenizer.hasMoreElements()) {
                        String node = stringTokenizer.nextToken();

                        if (Common.isBlank(node)) continue;
                        builder.append("/");
                        if (node.startsWith("{") && node.endsWith("}")) {
                            //参数
                            String paramName = new String(node.toCharArray(), 1, node.length() - 2);
                            for (int i = 0; i < unit.getParameters().length; i++) {
                                Param param = unit.getParameters()[i];

                                if (paramName.equals(param.getName())) {
                                    node = toStr(args[i]);
                                    break;
                                }
                            }
                        }
                        builder.append(URLEncoder.encode(node, "UTF-8"));
                    }
                    path = path + builder.toString();

                    // 找需要提交的对象
                    Object toSubmit = null;
                    if (args != null) {
                        // 2017-03-09 修复java客户端BigString Post问题
                        for (int i = 0; i < unit.getParameters().length; i++) {
                            if (args[i] == null) continue;
                            Param param = unit.getParameters()[i];
                            if (!JaxRSHelper.isPrimitive(param.getType()) || JaxRSHelper.isBigString(param)) {
                                toSubmit = args[i];
                                break;
                            }
                        }
//                for (Object o : args) {
//                    if (o != null && !JaxRSHelper.isPrimitive(o.getClass())) {
//                        toSubmit = o;
//                        break;
//                    }
//                }
                    }
                    return invoke(path, unit, toSubmit);
                }
            }
        };
    }


    protected abstract Object invoke(String path, Unit unit, Object toSubmit);

    protected String toStr(Object o) {
        if (o == null) return null;
        if (JaxRSHelper.isPrimitive(o.getClass())) return o.toString();
        return getJSONSerializer().toJson(o);
    }


//    protected abstract Object call(Module module, Unit unit, Object[] args) throws Throwable;
}
