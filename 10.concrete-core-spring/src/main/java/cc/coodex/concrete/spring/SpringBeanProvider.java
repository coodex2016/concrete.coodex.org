package cc.coodex.concrete.spring;

import cc.coodex.concrete.common.AbstractBeanProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class SpringBeanProvider extends AbstractBeanProvider implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

////    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T getBean(String name) {
//        return (T) context.getBean(name);
//    }
//
////    @Override
//    public <T> T getBean(Class<T> type, String name) {
//        return context.getBeansOfType(type).get(name);
//    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

}
