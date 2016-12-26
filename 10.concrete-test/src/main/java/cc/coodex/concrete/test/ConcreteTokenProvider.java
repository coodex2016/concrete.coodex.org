package cc.coodex.concrete.test;

import cc.coodex.concrete.common.BeanProviderFacade;
import cc.coodex.concrete.common.Token;
import cc.coodex.concrete.core.token.TokenManager;
import cc.coodex.concrete.core.token.local.LocalTokenManager;
import cc.coodex.util.Common;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.HashMap;
//import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-06.
 */
public class ConcreteTokenProvider {

    private final static Logger log = LoggerFactory.getLogger(ConcreteTokenProvider.class);

    private final static TokenManager TOKEN_MANAGER_INSTANCE = getInstance();

    private static TokenManager getInstance() {
        try {
            return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class);
        } catch (Throwable throwable) {
            log.warn("{}", throwable.getLocalizedMessage(), throwable);
            return new LocalTokenManager();
        }
    }


    public static Token getToken(String id) {
        return TOKEN_MANAGER_INSTANCE.getToken(Common.isBlank(id) ? Common.getUUIDStr() : id, true);
    }

    public static Token getToken(Description description) {
        TokenID testToken = description.getAnnotation(TokenID.class);
        return getToken(testToken == null ? null : testToken.value());
    }

}
