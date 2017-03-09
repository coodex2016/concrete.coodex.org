package cc.coodex.util;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class AcceptableServiceSPIFacade<Param_Type, T extends AcceptableService<Param_Type>> extends SPIFacade<T> {

    public T getServiceInstance(Param_Type param) {
        for (T instance : getAllInstances()) {
            if (instance.accept(param))
                return instance;
        }
        T instance = getDefaultProvider();
        if (instance.accept(param))
            return instance;
        throw new RuntimeException("no service instance accept this: " + param);
    }
}
