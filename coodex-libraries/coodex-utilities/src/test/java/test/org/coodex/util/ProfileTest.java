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

import org.coodex.util.Profile;
import org.junit.Assert;
import org.junit.Test;

public class ProfileTest {

    @Test
    public void test1(){
        System.setProperty("coodex.active.profiles","t1,t2,t3");
        Profile p1 = Profile.get("a1");
        Assert.assertEquals(10,p1.getInt("a1.t0"));
        Assert.assertEquals(11,p1.getInt("a1.t1"));
        Assert.assertEquals(12,p1.getInt("a1.t2"));
        Assert.assertEquals(13,p1.getInt("a1.t3"));
        p1 = Profile.get("b1");
        Assert.assertEquals(333,p1.getInt("b1.t0"));
        Assert.assertEquals(333,p1.getInt("b1.t1"));
        Assert.assertEquals(333,p1.getInt("b1.t2"));
        Assert.assertEquals(333,p1.getInt("b1.t3"));
    }

    public static void main(String[] args) {


    }
}
