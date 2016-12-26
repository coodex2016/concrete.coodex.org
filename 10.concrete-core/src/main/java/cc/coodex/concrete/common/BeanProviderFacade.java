package cc.coodex.concrete.common;

import cc.coodex.util.ReflectHelper;
import cc.coodex.util.SPIFacade;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class BeanProviderFacade extends ConcreteSPIFacade<BeanProvider> {

    private static final BeanProvider DEFAULT_PROVIDER = ReflectHelper.throwExceptionObject(
            BeanProvider.class, new ConcreteException(ErrorCodes.NO_BEAN_PROVIDER_FOUND));

    @Override
    protected BeanProvider getDefaultProvider() {
        return DEFAULT_PROVIDER;
    }

    private static final SPIFacade<BeanProvider> SPI_INSTANCE = new BeanProviderFacade();

    public static BeanProvider getBeanProvider() {
        return SPI_INSTANCE.getInstance();
    }
}
