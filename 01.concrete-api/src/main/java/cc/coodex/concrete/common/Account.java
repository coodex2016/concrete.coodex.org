package cc.coodex.concrete.common;

import java.io.Serializable;
import java.util.Set;

/**
 * 代表系统的一个账户
 * Created by davidoff shen on 2016-09-01.
 */
public interface Account<ID extends Serializable> {
    /**
     * 帐号ID
     *
     * @return
     */
    ID getId();

    /**
     * 帐号所拥有的角色
     *
     * @return
     */
    Set<String> getRoles();

    /**
     * 是否有效
     *
     * @return
     */
    boolean isValid();

//    /**
//     * 是否可信
//     *
//     * @return
//     */
//    boolean isCredibled();


}
