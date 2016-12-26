package cc.coodex.concrete.common;

import java.io.Serializable;

/**
 * Created by davidoff shen on 2016-11-22.
 */
public interface AccountFactory {

    <ID extends Serializable> Account<ID> getAccountByID(ID id);
}
