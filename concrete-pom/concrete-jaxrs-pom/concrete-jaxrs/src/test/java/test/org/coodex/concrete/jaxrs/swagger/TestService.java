/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.concrete.jaxrs.swagger;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.pojo.PageRequest;
import org.coodex.concrete.api.pojo.PageResult;
import org.coodex.mock.Mock;

import java.util.List;

@ConcreteService("test")
@Description(name = "测试模块", description = "swagger测试")
public interface TestService {

    @Description(name = "测试1", description = "看看我在哪")
    List<Byte> test1(
            @Description(name = "一个尖", description = "要不起") A obj);

    @ConcreteService("/{param}")
    String test2(String param);

    @ConcreteService("/{p1}/test3")
    int test3(
            @Description(name = "你", description = "你就是你") Integer p1,
            @Description(name = "我", description = "我就是我") String p2,
            @Description(name = "他", description = "他就是他") List<Integer> p3);

    PageResult<A> test4(PageRequest<List<A>> request);

    class A {
        @Mock.String(range = {"a", "b"})
        private String a;
        @Mock.Number("[1,10]")
        private Integer b;
        private byte[] c;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public Integer getB() {
            return b;
        }

        public void setB(Integer b) {
            this.b = b;
        }

        public byte[] getC() {
            return c;
        }

        public void setC(byte[] c) {
            this.c = c;
        }
    }
}
