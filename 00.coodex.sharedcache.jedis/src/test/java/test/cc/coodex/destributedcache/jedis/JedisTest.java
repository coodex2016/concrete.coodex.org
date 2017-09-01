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

package test.org.coodex.destributedcache.jedis;

import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientManager;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class JedisTest {
    public static void main(String [] args){
        SharedCacheClient client = SharedCacheClientManager.getClient("jedis");
        client.put("test1", "test1");
        client.put("test2", "test2", 1);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(client.get("test1"));
        System.out.println(client.get("test2"));
    }
}
