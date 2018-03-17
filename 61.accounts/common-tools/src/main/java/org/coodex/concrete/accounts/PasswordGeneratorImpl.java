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

package org.coodex.concrete.accounts;


import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.DigestHelper;

/**
 * 默认密码为p@55w0rd，可通过concrete.properties的defaultPassword重载
 * 编码方式为sha1散列
 * Created by davidoff shen on 2017-05-03.
 */
public class PasswordGeneratorImpl implements PasswordGenerator {
    @Override
    public boolean accept(String param) {
        return true;
    }

    @Override
    public String generate() {
        return ConcreteHelper.getProfile().getString("defaultPassword", "p@55w0rd");
    }

    @Override
    public String encode(String pwd) {
        if(pwd == null) pwd = generate();
        return DigestHelper.sha1(pwd.getBytes());
    }
}
