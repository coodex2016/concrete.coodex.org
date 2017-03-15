package org.coodex.concrete.common;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * 令牌
 * Created by davidoff shen on 2016-08-31.
 */
public interface Token extends Serializable {

    /**
     * 令牌创建时间
     *
     * @return
     */
    long created();

    /**
     * 是否还有效
     *
     * @return
     */
    boolean isValid();

    /**
     * 声明令牌失效
     */
    void invalidate();

    /**
     * 失效事件，此时应清空全部缓存的数据
     */
    void onInvalidate();


    /**
     * 当前账户
     *
     * @return
     */
    <ID extends Serializable> Account<ID> currentAccount();

    /**
     * 设置当前账户
     *
     * @param account
     */
    void setAccount(Account account);

    /**
     * 当前账户是否可信
     *
     * @return
     */
    boolean isAccountCredible();

    /**
     * 设置账户是否可信
     *
     * @param credible
     */
    void setAccountCredible(boolean credible);


    /**
     * 获取令牌id
     *
     * @return
     */
    String getTokenId();

    /**
     * 从令牌中获取属性
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T getAttribute(String key);

    /**
     * 将属性缓存到令牌中
     *
     * @param key
     * @param attribute
     */
    void setAttribute(String key, Serializable attribute);

    /**
     * 从令牌中移除属性
     *
     * @param key
     */
    void removeAttribute(String key);

    /**
     * 遍历所有属性
     *
     * @return
     */
    Enumeration<String> attributeNames();


    /**
     * 更新token内的值
     */
    void flush();


}
