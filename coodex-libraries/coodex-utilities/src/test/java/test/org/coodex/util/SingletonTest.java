/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.SingletonMap;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SingletonTest {

    private final static Logger log = LoggerFactory.getLogger(SingletonTest.class);

    private static final SingletonMap<Integer/*键类型*/, String/*值类型*/> SINGLETON_MAP
            = SingletonMap.<Integer, String>builder()
            // 默认的值构建function
            .function(String::valueOf)
            // 最大缓存时间，单位毫秒，<=0则表示不超期
            .maxAge(0)
            // 当已创建的实例被取出时，是否更新他的激活时间，如果maxAge为非0值，则会从此刻起重新计算生命周期，默认false
            .activeOnGet(false)
            // 当实例挂掉的时候，会触发deathListener
            .deathListener((k, v) -> {
            })
            //map的构建器，默认用ConcurrentHashMap
            .mapSupplier(ConcurrentHashMap::new)
            // 不同的map对null作为键值的处理方式不同，为了适配多种map的实现，可以指定nullKey等同于哪个key
            .nullKey(Integer.MIN_VALUE)
            // 管理值对象生命周期的线程池
            .scheduledExecutorService(ExecutorsHelper.newSingleThreadScheduledExecutor("test"))
            .build();

    @Test
    public void test1(){
        log.info(SINGLETON_MAP.get(null));

        //1秒后失效
        log.info(SINGLETON_MAP.get(1, 1000));

        //使用非默认function来构建值，使用Function<KEY,VALUE>类似
        log.info(SINGLETON_MAP.get(2, () -> "hello coodex."));
        // 因为2的值已存在，所以看到的还是hello coodex
        log.info(SINGLETON_MAP.get(2, () -> "can u see me?"));

        // 使用非默认的deathListener
        log.info(SINGLETON_MAP.get(3, 500, (i, s) -> System.out.println("key: " + i + ", value: " + s)));
    }
}
