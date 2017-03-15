package org.coodex.concrete.core.token;

import org.coodex.closure.threadlocals.StackClosureThreadLocal;
import org.coodex.concrete.common.*;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * 基于当前线程上下文提供<s>Session</s> Token
 * <i><s>TO</s><s>DO: 考虑到需要支持servlet 3.0和其他需要异步的i/o提供者，需要支持异步获取会话</s></i>
 * 上述内容无需由TokenWrapper考虑，应有I/O服务提供者层提供
 * Created by davidoff shen on 2016-09-05.
 */
public class TokenWrapper implements Token {

//    private final static TokenManager LOCAL_TOKEN_MANAGER = new LocalTokenManager();
//
//    private final static ConcreteSPIFacade<TokenManager> TOKEN_MANAGER_SPI_FACADE = new ConcreteSPIFacade<TokenManager>() {
//        @Override
//        protected TokenManager getDefaultProvider() {
//            return LOCAL_TOKEN_MANAGER;
//        }
//    };
//
//    public static final TokenManager getTokenManager() {
//        return TOKEN_MANAGER_SPI_FACADE.getBeanProvider();
//    }


    //////////////////

    private static final StackClosureThreadLocal<Token> closure = new StackClosureThreadLocal<Token>();

    private static final Token singletonInstance = new TokenWrapper();

    private Token getToken() {
        return getToken(true);
    }

    private Token getToken(boolean checkValidation) {
        Token token = closure.get();
        Assert.isNull(token, ErrorCodes.NONE_TOKEN);
        Assert.is(checkValidation && !token.isValid(), ErrorCodes.TOKEN_INVALIDATE, token.getTokenId());
        return token;
    }

    public static final Token getInstance() {
        return singletonInstance;
    }

    public static final Object closure(Token token, ConcreteClosure runnable) {
        return closure.runWith(token, runnable);
    }


    @Override
    public long created() {
        return getToken().created();
    }

    @Override
    public boolean isValid() {
        return getToken(false).isValid();
    }

    @Override
    public void invalidate() {
        getToken().invalidate();
    }

    @Override
    public void onInvalidate() {
        getToken().onInvalidate();
    }

    @Override
    public <ID extends Serializable> Account<ID> currentAccount() {
        return getToken().currentAccount();
    }

    @Override
    public void setAccount(Account account) {
        getToken().setAccount(account);
    }

    @Override
    public boolean isAccountCredible() {
        return getToken().isAccountCredible();
    }

    @Override
    public void setAccountCredible(boolean credible) {
        getToken().setAccountCredible(credible);
    }

    @Override
    public String getTokenId() {
        return getToken(false).getTokenId();
    }

    @Override
    public <T> T getAttribute(String key) {
        return getToken().getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Serializable attribute) {
        getToken().setAttribute(key, attribute);
    }

    @Override
    public void removeAttribute(String key) {
        getToken().removeAttribute(key);
    }

    @Override
    public Enumeration<String> attributeNames() {
        return getToken().attributeNames();
    }

    @Override
    public void flush() {
        getToken().flush();
    }
}
