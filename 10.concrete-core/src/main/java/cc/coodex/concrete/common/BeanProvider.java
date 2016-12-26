package cc.coodex.concrete.common;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public interface BeanProvider {

    <T> T getBean(Class<T> type);

//    <T> T getBean(String name);

//    <T> T getBean(Class<T> type, String name);

    <T> Map<String, T> getBeansOfType(Class<T> type);

}
