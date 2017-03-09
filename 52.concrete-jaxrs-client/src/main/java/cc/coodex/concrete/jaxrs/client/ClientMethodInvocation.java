package cc.coodex.concrete.jaxrs.client;

import cc.coodex.concrete.jaxrs.struct.Unit;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class ClientMethodInvocation implements MethodInvocation {

    private final Unit unit;
    private final Object _this;
    private final Object[] arguments;

    public ClientMethodInvocation(Object _this, Unit unit, Object[] arguments) {
        this.unit = unit;
        this._this = _this;
        this.arguments = arguments;
    }

    @Override
    public Method getMethod() {
        return unit.getMethod();
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getThis() {
        return _this;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return null;
    }

}
