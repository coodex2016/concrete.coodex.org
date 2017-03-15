package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.MicroService;

/**
 * Created by davidoff shen on 2017-02-09.
 */
@MicroService("Calc")
public interface Calc extends ConcreteService {

    @Description(name = "求和", description = "求X + Y = ")
    int add(@Description(name = "被加数", description = "被加数") int x,
            @Description(name = "加数", description = "加数") int y);
}
