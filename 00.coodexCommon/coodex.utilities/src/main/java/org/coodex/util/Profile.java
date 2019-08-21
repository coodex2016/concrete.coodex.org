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
package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
public abstract class Profile {

    private final static Logger log = LoggerFactory.getLogger(Profile.class);

    private static final AcceptableServiceLoader<URL, ProfileProvider> PROFILE_PROVIDER_LOADER =
            new AcceptableServiceLoader<URL, ProfileProvider>() {
            };

    private static final Singleton<String[]> ALL_SUPPORTED_FILE_EXT = new Singleton<String[]>(
            new Singleton.Builder<String[]>() {
                @Override
                public String[] build() {
                    ProfileProvider[] profileProviders = PROFILE_PROVIDER_LOADER.getAll().values().toArray(new ProfileProvider[0]);
                    Arrays.sort(profileProviders);
                    List<String> list = new ArrayList<String>();
                    for (ProfileProvider profileProvider : profileProviders) {
                        if (profileProvider.isAvailable()) {
                            list.addAll(Arrays.asList(profileProvider.getSupported()));
                        }
                    }
                    return list.toArray(new String[0]);
                }
            }
    );
    private static final Set<String> NOT_FOUNDS = new HashSet<String>();
    //    private static final String DEFAULT_KEY = Common.getUUIDStr();
    private static final URL DEFAULT_URL;
//    private static final String YAML_CLASS = "org.yaml.snakeyaml.Yaml";
//    private static Boolean yamlFirst = null;

    private static final String RELOAD_INTERVAL = System.getProperty("Profile.reloadInterval");
    //    @Deprecated
//    private static final SingletonMap<String, Profile> PROFILES = new SingletonMap<String, Profile>(
//            new SingletonMap.Builder<String, Profile>() {
//
//
//                @Override
//                public Profile build(String key) {
//                    if (DEFAULT_KEY.equals(key)) return new NullProfile();
//
//                    if (key.toLowerCase().endsWith(".properties")) {
//                        return new ProfileBaseProperties(key);
//                    } else if (key.toLowerCase().endsWith(".yaml")) {
//                        return new ProfileBaseYaml(key);
//                    } else {
//                        return PROFILES.get(findPath(Common.trim(key, ":/\\.")));
//                    }
//                }
//            },
//            toLong(RELOAD_INTERVAL, 0L) * 1000L
//    );
    private static SingletonMap<URL, Profile> URL_PROFILES_MAP = new SingletonMap<URL, Profile>(
            new SingletonMap.Builder<URL, Profile>() {
                @Override
                public Profile build(URL key) {
                    if (key == null)
                        throw new NullPointerException("profile url could not be null.");
                    ProfileProvider profileProvider = PROFILE_PROVIDER_LOADER.select(key);
                    if (profileProvider == null) {
                        return new NullProfile();
                    } else {
                        return profileProvider.get(key);
                    }
//                    String resourceName = key.toString();
//                    if (resourceName.endsWith(".properties")) {
//                        return new ProfileBaseProperties(key);
//                    } else if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
//                        if (!isYamlFirst()) {
//                            log.warn("YAML not support. class {} not found. {}", YAML_CLASS, key.toString());
//                            return new NullProfile();
//                        } else
//                            return new ProfileBaseYaml(key);
//                    } else {
//                        return new NullProfile();
//                    }
                }
            }, toLong(RELOAD_INTERVAL, 0L) * 1000L
    );

    public static String[] allSupportedFileExt() {
        return ALL_SUPPORTED_FILE_EXT.get();
    }

    static {
        try {
            DEFAULT_URL = new URL("file:/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private static URL findPath(String path) {
        String[] ex = allSupportedFileExt();
        //isYamlFirst() ? new String[]{".yml", ".yaml", ".properties"} : new String[]{".properties"};
        for (String s : ex) {
            if (path.endsWith(s)) {
                URL x = Common.getResource(path);
                if (x != null) return x;
            }
            URL x = Common.getResource(path + s);
            if (x != null) return x;
        }
        if (!NOT_FOUNDS.contains(path)) {
            synchronized (NOT_FOUNDS) {
                if (!NOT_FOUNDS.contains(path)) {
                    if (log.isInfoEnabled()) {
                        StringBuilder builder = new StringBuilder("Profile ")
                                .append(path).append(" not found.[");
                        for (int i = 0; i < ex.length; i++) {
                            if (i > 0) {
                                builder.append(", ");
                            }
                            builder.append("'").append(ex[i]).append("'");
                        }
                        builder.append("]");
                        log.info(builder.toString());
                    }
                    NOT_FOUNDS.add(path);
                }
            }
        }
        return DEFAULT_URL;
    }

//    private static boolean isYamlFirst() {
//        if (yamlFirst == null) {
//            try {
//                Class.forName(YAML_CLASS);
//                yamlFirst = true;
//            } catch (ClassNotFoundException e) {
//                yamlFirst = false;
//            }
//        }
//        return yamlFirst;
//    }

    /**
     * 根据url获取资源
     *
     * @param url 资源url
     * @return profile
     */
    public static Profile get(URL url) {
        return URL_PROFILES_MAP.get(url);
    }

    public static Profile get(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }

        return get(findPath(path));
    }


    @Deprecated
    public static Profile getProfile(URL url) {
        return get(url);
    }

    @Deprecated
    public static Profile getProfile(String path) {
        return get(path);
//        Profile p = profiles.get(path);
//        if (p == null) {
//            p = new Profile(path);
//            profiles.put(path, p);
//        }
//        return p;
    }
//    //    private static ScheduledExecutorService RELOAD_POOL;
//    private static Singleton<ScheduledExecutorService> RELOAD_POOL =
//            new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
//                @Override
//                public ScheduledExecutorService build() {
//                    return ExecutorsHelper.newSingleThreadScheduledExecutor("profile_reload");
//                }
//            });
//    private static Set<String> notFound = new HashSet<String>();
//    protected Properties p = new Properties();
//    private long lastModified = 0;
//    private String resourcePath;
//    private File f;
//    private String location;
//    private InputStream is;
//
//    private Runnable reloadTask = new Runnable() {
//        public void run() {
//
//            try {
//                if (f == null) {
//                    loadFromPath(resourcePath, true);
//                } else if (f.lastModified() != lastModified) {
//                    lastModified = f.lastModified();
//                    try {
//                        is = new FileInputStream(f);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    load();
//                }
//            } finally {
//                submitReloadTask();
//            }
//        }
//    };
//
//    protected Profile(String path) {
//        this.resourcePath = path;
//        loadFromPath(path);
//        submitReloadTask();
//    }

//    private static ScheduledExecutorService getReloadPool() {
////        if (RELOAD_POOL == null) {
////            synchronized (Profile.class) {
////                if (RELOAD_POOL == null)
////                    RELOAD_POOL = ExecutorsHelper.newSingleThreadScheduledExecutor();
////            }
////        }
////        return RELOAD_POOL;
//        return RELOAD_POOL.getInstance();
//    }

    public boolean getBool(String key, boolean v) {
        return toBool(getString(key), v);
    }

//    public static InputStream getResourceAsStream(String resourcePath) throws IOException {
//        URL url = getResource(Common.trim(resourcePath, '/'));
//        return url == null ? new FileInputStream(resourcePath) : url.openStream();
//    }

//    static public URL getResource(String resource) {
//        return Common.getResource(resource);
//    }

//    private void loadFromPath(String path) {
//        loadFromPath(path, false);
//    }

//    private void submitReloadTask() {
//        long inteval = -1;
//        try {
//            inteval = Long.parseLong(RELOAD_INTERVAL);
//        } catch (Throwable t) {
//        }
//
//        if (inteval > 0) {
//            ScheduledExecutorService pool = getReloadPool();
//            if (pool.isShutdown() || pool.isTerminated()) return;
//            pool.schedule(reloadTask, inteval, TimeUnit.SECONDS);
//        }
//
//    }

//    private synchronized void loadFromPath(String path, boolean reload) {
//        if (!path.startsWith("/"))
//            path = "/" + path;
//        URL uri = Profile.class.getResource(path);
//
//        if (uri == null) {
//            while (path.startsWith("/"))
//                path = path.substring(1);
//
//            uri = getResource(path);
//        }
//
//        if (uri != null) {
//            location = uri.toString();
//            if (location.indexOf('!') >= 0) {
//                f = null;
//                is = Profile.class.getResourceAsStream(path);
//            } else {
//                f = new File(uri.getFile());
//                try {
//                    is = new FileInputStream(f);
//                    lastModified = f.lastModified();
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            load();
//        } else {
//            if (!reload) {
//                if (!notFound.contains(path)) {
//                    notFound.add(path);
//                    log.info("Profile [" + path + "] not found.");
//                }
//            }
//        }
//    }

//    public String getLocation() {
//        return location;
//    }

//    public Properties getProperties() {
//        return p;
//    }


//    private void load() {
//        try {
//            p.clear();
//            if (is != null) {
//                p.load(is);
//                is.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public boolean getBool(String key) {
        return getBool(key, false);
    }

    protected abstract String getStringImpl(String key);

    private boolean isPlaceHolder(String v) {
        return v.startsWith("${") && v.endsWith("}");

    }

    private String actualValue(String v) {
        if (isPlaceHolder(v)) {
            String x = v.substring(2, v.length() - 1);
            int index = x.indexOf(':');
            String namespace = null;
            String key = x;
            if (index > 0) {
                namespace = x.substring(0, index);
                key = x.substring(index + 1);
            }
            Profile profile = namespace == null ? this : Profile.get(namespace);

            return profile.getString(key);
        }
        return v;
    }

    public String getString(String key, String v) {
        String s = getStringImpl(key);
        return s == null ? v : actualValue(s);
    }

//    private boolean isPlaceHolder(String v) {
//        return v.startsWith("${") && v.endsWith("}");
//
//    }

//    private String actualValue(String v) {
//        if (isPlaceHolder(v)) {
//            String x = v.substring(2, v.length() - 1);
//            int index = x.indexOf(':');
//            String namespace = null;
//            String key = x;
//            if(index > 0){
//                namespace = x.substring(0,index);
//                key = x.substring(index + 1);
//            }
//            Profile profile = namespace == null ? this : Profile.getProfile(namespace + ".properties");
//
//            return profile.getString(key);
//        }
//        return v;
//    }

    private static class NullProfile extends Profile {
        @Override
        protected String getStringImpl(String key) {
            return null;
        }
    }
//    {
//        String s = p.getProperty(key);
//        return s == null ? v : actualValue(s);
//    }


    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int v) {
        return toInt(getString(key), v);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long v) {
        return toLong(getString(key), v);
    }

    public String[] getStrList(String key) {
        return getStrList(key, ",");
    }

    public String[] getStrList(String key, String delim) {
        return getStrList(key, delim, null);
    }

    public String[] getStrList(String key, String delim, String[] v) {
        return toArray(getString(key), delim, v);
    }

}
