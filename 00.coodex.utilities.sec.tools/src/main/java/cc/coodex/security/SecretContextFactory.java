package cc.coodex.security;

/**
 * 上下文工厂
 * Created by davidoff shen on 2017-02-05.
 */
public interface SecretContextFactory {

    SecretContext getContext(String driver);
}
