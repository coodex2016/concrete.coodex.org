package org.coodex.cache;

public interface Cache {

    // 存储节点
    String getNode();

    // 名称
    String getName();

    int cachedItems();

    byte[] next();

    void cache(byte[] bytes);

    void cache(byte[] data, int offset, int length);

}
