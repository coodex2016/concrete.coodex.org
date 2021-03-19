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
package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.coodex.util.Common.getResource;

/**
 * 自财运通项目中移植过来utilities中<br>
 * 2016/09/05 废弃<S>
 * 2014/04/27 支持命名空间<br>
 * 配置如下:<br>
 * A.properties<br>
 * org.coodex.util.profile.NAMESPACE=XX.properties<br>
 * <br>
 * XX.properties<br>
 * abcd=xxxxxx<br>
 * <br>
 * 则相当于在A.properties中<br>
 * NAMESPACE.abcd=xxxxxx<br></S>
 * <p>
 * <p>
 * 2016-08-30
 * 1、废弃基于File类型的构造
 * 2、每个资源仅维持一个实例
 * 2016-09-05
 * 1、废弃命名空间的支持
 * 2、修改监测机制
 * Profile.reloadInterval 用以制定重新加载的间隔时间，单位为秒
 *
 * @author davidoff
 * @version v1.0 2014-03-18
 */
public class ProfileBaseProperties extends Profile {

    private static final Logger log = LoggerFactory.getLogger(ProfileBaseProperties.class);

    protected Properties p = new Properties();

    private File f;
    private String location;
    private InputStream is;


    @Deprecated
    ProfileBaseProperties(String path) {
        try {
            loadFromPath(path);
        } catch (IOException e) {
            log.warn("{} load failed.", path);
        }
    }

    ProfileBaseProperties(URL url) {
        try {
            is = url.openStream();
            load();
        } catch (IOException e) {
            log.warn("{} load failed.", url);
        }
    }


    private void loadFromPath(String path) throws IOException {
        if (!path.startsWith("/"))
            path = "/" + path;
        URL uri = ProfileBaseProperties.class.getResource(path);

        if (uri == null) {
            uri = getResource(Common.trim(path, "/\\."));
        }

        if (uri != null) {
//            location = uri.toString();
            is = uri.openStream();
//            if (location.indexOf('!') >= 0) {
//                f = null;
//                is = ProfileBaseProperties.class.getResourceAsStream(path);
//            } else {
//                f = new File(uri.getFile());
//                try {
//                    is = new FileInputStream(f);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            }
            load();
        }
    }


    private void load() {
        try {
            p.clear();
            if (is != null) {
                p.load(is);
                is.close();
            }
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
    }


    @Override
    protected String getStringImpl(String key) {
        return p.getProperty(key);
    }

    @Override
    protected boolean isNull(String key) {
        return p.getProperty(key) == null;
    }


}
