package test.org.coodex.intf.pojo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by davidoff shen on 2016-09-03.
 */
public class BV  {

    @NotNull(message = "ok cannot be NULL!")
    private String ok;


    @Valid
    @NotNull(message = "bvx cannot be NULL")
    private BV bvx = null;

    public String getOk() {
        return ok;
    }

//    public BV getBvx() {
//        return bvx;
//    }

    public BV(String ok) {
        this(ok, new BV(ok, null));
    }

    BV(String ok, BV bv){
        this.ok = ok;
        this.bvx = bv;
    }
}
