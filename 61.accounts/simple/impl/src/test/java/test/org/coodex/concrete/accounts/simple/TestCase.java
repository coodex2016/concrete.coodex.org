/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.coodex.concrete.accounts.simple;

import org.apache.commons.codec.binary.Base32;
import org.coodex.concrete.accounts.TOTPAuthenticator;
import org.coodex.concrete.accounts.simple.api.Login;
import org.coodex.concrete.test.ConcreteTestCase;
import org.coodex.util.Clock;
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
                                Clock.currentTimeMillis() / TimeUnit.SECONDS.toMillis(30))));
    }
}
