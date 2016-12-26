package cc.coodex.concrete.common.struct;

import cc.coodex.concrete.api.*;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class AbstractModule<UNIT extends AbstractUnit> implements Annotated, Comparable<AbstractModule> {

    private Class<?> interfaceClass;

    public AbstractModule(Class<?> interfaceClass) {
        if (ConcreteService.class.isAssignableFrom(interfaceClass)
                && interfaceClass.getAnnotation(MicroService.class) != null
                && interfaceClass.getAnnotation(Abstract.class) == null) {
            this.interfaceClass = interfaceClass;
        } else {
            throw new RuntimeException(interfaceClass.getName() + " is NOT a concrete ConcreteService");
        }
    }

    /**
     * 业务模块继承链
     *
     * @return
     */
    public abstract List<Class<?>> getInheritedChain();

    /**
     * 服务模块所在的class
     *
     * @return
     */
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    /**
     * 服务模块的角色属主
     *
     * @return
     */
    public Domain getDomain() {
        return getAnnotation(Domain.class);
    }

    private Description getDesc() {
        return getAnnotation(Description.class);
    }

    /**
     * 服务模块名称
     *
     * @return
     */
    public abstract String getName();

    /**
     * 文档化的标题
     *
     * @return
     */
    public String getLabel() {
        return getDesc() == null ? getName() : getDesc().name();
    }

    /**
     * 服务模块说明
     *
     * @return
     */
    public String getDescription() {
        return getDesc() == null ? null : getDesc().description();
    }

    /**
     * 所有的服务原子
     *
     * @return
     */
    public abstract UNIT[] getUnits();

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return interfaceClass.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return interfaceClass.getAnnotations();
    }
}
