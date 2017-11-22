/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.concrete.messages;

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.common.messages.*;
import org.coodex.concrete.core.messages.MessageHelper;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.GenericType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageTest {
    public static void main(String [] args){
        PostOffice postOffice = MessageHelper.getPostOffice();

        Subscriber<Body1> body1Subscriber = postOffice.subscribe(new AbstractSubscription<Body1>("1",new Filter1()){

        });
        Subscriber<List<Body2>> listSubscriber = postOffice.subscribe(new AbstractSubscription<List<Body2>>("1",new Filter2()){});
        Subscriber<Map<String,List<Body1>>> mapSubscriber = postOffice.subscribe(new AbstractSubscription<Map<String, List<Body1>>>("3",null) {
        });
        postOffice.cancel(body1Subscriber);
        postOffice.postMessage("1", new Body1());

        postOffice.postMessage("1", MockerFacade.mock(new GenericType<List<Body2>>() {
        }));
        System.out.println("sadfasfd");
        postOffice.postMessage("1", MockerFacade.mock(Body2.class));
//        postOffice.postMessage("3", MockerFacade.mock(new HashMap<String,List<Body1>>()));
    }


}
