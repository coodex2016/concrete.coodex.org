package cc.coodex.practice.jaxrs.api;

import cc.coodex.concrete.api.MicroService;
import cc.coodex.concrete.api.ServiceTiming;
import cc.coodex.practice.jaxrs.pojo.Book;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService("book")
@ServiceTiming("rule2")
public interface ServiceExample extends ServiceB {

    String tokenId();
}
