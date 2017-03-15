package org.coodex.concrete.core.token;

import org.coodex.concrete.common.Token;

/**
 * Created by davidoff shen on 2016-11-02.
 */
public interface TokenManager {

    long DEFAULT_MAX_IDLE = 60; //默认60分钟
    /**
     * 获取一个已存在的令牌，令牌不存在返回空值
     *
     * @param id
     * @return
     */
    Token getToken(String id);

    /**
     * 获取一个令牌，若该id令牌不存在且force为真是，则创建一个
     *
     * @param id
     * @param force
     * @return
     */
    Token getToken(String id, boolean force);
}
