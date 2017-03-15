package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.ServiceTiming;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService("book")

public interface ServiceExample extends ServiceB {
    @ServiceTiming("rule2")
    String tokenId();
}
