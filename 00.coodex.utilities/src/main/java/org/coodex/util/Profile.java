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
package org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.coodex.util.Common.*;

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
public class Profile implements StringMap{

    private static ScheduledExecutorService RELOAD_POOL;

    private static final String RELOAD_INTERVAL = System.getProperty("Profile.reloadInterval");

    private static final Logger log = LoggerFactory.getLogger(Profile.class);

    private static synchronized ScheduledExecutorService getReloadPool() {
        if (RELOAD_POOL == null) {
            RELOAD_POOL = ExecutorsHelper.newSingleThreadScheduledExecutor();
        }
        return RELOAD_POOL;
    }

    private static final Map<String, Profile> profiles = new HashMap<String, Profile>();

    public synchronized static Profile getProfile(String path) {
        Profile p = profiles.get(path);
        if (p == null) {
            p = new Profile(path);
            profiles.put(path, p);
        }
        return p;
    }

    private long lastModified = 0;
    private String resourcePath;
    private File f;
    private String location;
    private InputStream is;

    protected Properties p = new Properties();


    protected Profile(String path) {
        this.resourcePath = path;
        loadFromPath(path);
        submitReloadTask();
    }

    private void loadFromPath(String path) {
        loadFromPath(path, false);
    }

    private Runnable reloadTask = new Runnable() {
        public void run() {

            try {
                if (f == null) {
                    loadFromPath(resourcePath, true);
                } else if (f.lastModified() != lastModified) {
                    lastModified = f.lastModified();
                    try {
                        is = new FileInputStream(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    load();
                }
            } finally {
                submitReloadTask();
            }

        }
    };


    private void submitReloadTask() {
        long inteval = -1;
        try {
            inteval = Long.parseLong(RELOAD_INTERVAL);
        } catch (Throwable t) {
        }

        if (inteval > 0) {
            ScheduledExecutorService pool = getReloadPool();
            if (pool.isShutdown() || pool.isTerminated()) return;
            pool.schedule(reloadTask, inteval, TimeUnit.SECONDS);
        }

    }

    private synchronized void loadFromPath(String path, boolean reload) {
        if (!path.startsWith("/"))
            path = "/" + path;
        URL uri = Profile.class.getResource(path);

        if (uri == null) {
            while (path.startsWith("/"))
                path = path.substring(1);

            uri = getResource(path);
        }

        if (uri != null) {
            location = uri.toString();
            if (location.indexOf('!') >= 0) {
                f = null;
                is = Profile.class.getResourceAsStream(path);
            } else {
                f = new File(uri.getFile());
                try {
                    is = new FileInputStream(f);
                    lastModified = f.lastModified();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            load();
        } else {
            if (!reload)
                log.warn("Profile [" + path + "] not found.");
        }
    }

    static public URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;

        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        // We could not find resource. Ler us now try with the
        // classloader that loaded this class.
        classLoader = Profile.class.getClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }
        return ClassLoader.getSystemResource(resource);
    }

    public String getLocation() {
        return location;
    }

    public Properties getProperties() {
        check();
        return p;
    }

    private synchronized void check() {
//        if (f == null) {
//            if (loaded)
//                return;
//            else {
//                load();
//            }
//        } else if (f.lastModified() != lastModified) {
//            lastModified = f.lastModified();
//            try {
//                is = new FileInputStream(f);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            load();
//
//        }
    }

    private void load() {
        try {
            p.clear();
            if (is != null) {
                p.load(is);
//                loaded = true;
                is.close();
            } else {
//                loaded = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void store(String comments) {

        if (f == null) {

            return;
        }
        try {

            p.store(new FileOutputStream(f), comments);
            lastModified = f.lastModified();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean v) {
        check();
        return toBool(p.getProperty(key), v);
//        String s = Common.nullToStr(p.getProperty(key));
//        if (s.equals("1") || s.equalsIgnoreCase("T")
//                || s.equalsIgnoreCase("TRUE"))
//            return true;
//        else if (s.equals("0") || s.equalsIgnoreCase("F")
//                || s.equalsIgnoreCase("FALSE"))
//            return false;
//        else
//            return v;
    }

    @Override
    public String getString(String key, String v) {
        check();
        String s = p.getProperty(key);
        if (s == null) {
            return resolve(key, v);
        }
        return s == null ? v : s;
    }

    private String resolve(String key, String v) {
        // 20160905 取消从其他资源文件里
//        String mykey = key;
//        int index;
//        String nameSpace = "";
//        while ((index = mykey.indexOf('.')) > 0) {
//            nameSpace += "." + mykey.substring(0, index);
//            mykey = mykey.substring(index + 1);
//            String path = p.getProperty(NAMESPACE_PRE + nameSpace, null);
//            if (path == null)
//                continue;
//            synchronized (nameSpaceProfiles) {
//                Profile profile = nameSpaceProfiles.get(nameSpace);
//                if (profile == null) {
//                    profile = Profile.getProfile(path);
//                    nameSpaceProfiles.put(nameSpace, profile);
//                }
//                return profile.getString(mykey, v);
//            }
//        }
        return v;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int v) {
//        String s = getString(key);
//        try {
//            return Integer.valueOf(s);
//        } catch (Exception e) {
//            return v;
//        }
        return toInt(getString(key), v);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long v) {
        return toLong(getString(key), v);
//        String s = getString(key);
//        try {
//            return Long.valueOf(s);
//        } catch (Exception e) {
//            return v;
//        }
    }

    public String[] getStrList(String key) {
        return getStrList(key, ",");
    }

    public String[] getStrList(String key, String delim) {
        return getStrList(key, delim, null);
    }

    public String[] getStrList(String key, String delim, String[] v) {
        check();
        return toArray(getString(key), delim, v);
//        String s = p.getProperty(key);
//        if (s == null)
//            return v;
//        StringTokenizer st = new StringTokenizer(s, delim, false);
//        List<String> list = new ArrayList<String>();
////        int count = 0;
//        while (st.hasMoreElements()) {
//            list.add(st.nextToken().trim());
////            st.nextElement();
////            count++;
//        }
////        st = new StringTokenizer(s, delim, false);
////        String[] result = new String[count];
////        for (int i = 0; i < count; i++)
////            result[i] = (String) st.nextElement();
//        return list.toArray(new String[0]);
    }

    public void setString(String key, String value) {
        setString(key, value, true);
    }

    public void setString(String key, String value, boolean acceptNull) {
        if (!acceptNull && value == null)
            return;
        setProperty(key, value, false);
    }

    public void setInt(String key, int value, int unchange) {
        if (value != unchange)
            setProperty(key, String.valueOf(value), false);
    }

    public void setLong(String key, long value, long unchange) {
        if (value != unchange)
            setProperty(key, String.valueOf(value), false);
    }

    public void setInt(String key, int value) {
        setProperty(key, String.valueOf(value), false);
    }

    public void setLong(String key, long value) {
        setProperty(key, String.valueOf(value), false);
    }

    private void setProperty(String key, String value, boolean store) {
        String s = getString(key);
        if (s == value)
            return;
        if (s != null && s.equals(value))
            return;
        p.setProperty(key, value == null ? "" : value);
        if (store)
            store("");
    }

    public synchronized void setProperty(String key, String value) {
        setProperty(key, value, true);
    }

    public void setStrList(String key, String[] list) {
        if (list == null) {
            setProperty(key, null);
            return;
        }
        String s = "";
        for (int i = 0; i < list.length; i++) {
            s += list[i] + ",";
        }
        setProperty(key, s);
    }

    public void setBool(String key, boolean v) {
        setProperty(key, v ? "true" : "false");
    }

}
