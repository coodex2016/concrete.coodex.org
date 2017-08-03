package test.org.coodex.intf;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.ServiceTiming;
import test.org.coodex.intf.pojo.BV;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by davidoff shen on 2016-09-02.
 */
@MicroService
public interface AspectTest extends ConcreteService {

//    @MicroService
//    @NotService
    @ServiceTiming({"rule1"})
    void test1(
            @NotNull(message = "参数不能为 null")
                    String a,
            @Valid
            @NotNull(message = "BV cannot be NULL.")
                    BV bv);
}
