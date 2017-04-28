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

import org.coodex.practice.jaxrs.api.Calc;
import org.coodex.practice.jaxrs.api.ServiceExample;

import static org.coodex.concrete.common.ConcreteHelper.inheritedChain;

/**
 * Created by davidoff shen on 2017-04-28.
 */
public class InheritedChainTest {

    public static void main(String [] args){
        for(Class c : inheritedChain(Calc.class, ServiceExample.class)){
            System.out.println(c.getName());
        }
    }
}
