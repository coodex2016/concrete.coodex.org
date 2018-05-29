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

package test.org.coodex.intf.impl;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.concurrent.ExecutorsHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import test.org.coodex.intf.AspectTest;
import test.org.coodex.intf.pojo.BV;

import java.text.MessageFormat;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class AspectTestImpl implements AspectTest {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        try {

            ErrorMessageFacade.register(ErrorCodes.class);
//            ErrorMessageFacade.registDefaultPattern(DATA_VIOLATION,"{0}");
//        ExecutorService executorService = ExecutorsHelper.newSingleThreadExecutor();
//
//        for(int i = 0; i < 10; i ++){
//            final int finalI = i;
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println(finalI);
//                }
//            });
//        }
            ApplicationContext context = new ClassPathXmlApplicationContext("test.xml");
//        ReloadableResourceBundleMessageSource a;
            MessageFormat mf;
//        a.getMessage()
            AspectTest test = context.getBean(AspectTest.class);
//            test.test1(null, null);
            test.test1("1", new BV(null));
            test.test1("1", new BV("2"));
        } catch (ConcreteException ce) {
            System.out.println("errorCode: " + ce.getCode());
        } catch (Throwable throwable) {
            System.out.println(throwable.getLocalizedMessage());
            throwable.printStackTrace();
        } finally {
            ExecutorsHelper.shutdownAllNOW();
        }

    }

    @Override
    public void test1(String a, BV bv) {
        System.out.println("executing test1");
    }
}
