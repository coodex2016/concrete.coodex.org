package test.org.coodex.concrete.accounts.simple;

import org.apache.commons.codec.binary.Base32;
import org.coodex.concrete.accounts.TOTPAuthenticator;
import org.coodex.concrete.accounts.simple.api.Login;
import org.coodex.concrete.test.ConcreteTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Created by davidoff shen on 2017-07-05.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test.xml")
public class TestCase extends ConcreteTestCase {

    @Inject
    private Login login;

    @Test
    public void test() throws InvalidKeyException, NoSuchAlgorithmException {
        login.login("coodex", "p@55w0rd",
                String.valueOf(
                        TOTPAuthenticator.buildCode(new Base32().decode("1234567890"),
                                System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(30))));
    }
}
