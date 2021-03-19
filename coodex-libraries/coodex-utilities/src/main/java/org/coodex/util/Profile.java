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

import org.coodex.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    final static Profile NULL_PROFILE = new NullProfile();
    private final static Logger log = LoggerFactory.getLogger(Profile.class);
    private static final LazySelectableServiceLoader<URL, ProfileProvider> PROFILE_PROVIDER_LOADER =
            new LazySelectableServiceLoader<URL, ProfileProvider>() {
            };
    private static final Singleton<String[]> ALL_SUPPORTED_FILE_EXT = Singleton.with(
            () -> {
//                ProfileProvider[] profileProviders = PROFILE_PROVIDER_LOADER.getAll().values().toArray(new ProfileProvider[0]);
//                Arrays.sort(profileProviders);
                List<String> list = new ArrayList<>();
                for (ProfileProvider profileProvider : PROFILE_PROVIDER_LOADER.sorted()) {
                    if (profileProvider.isAvailable()) {
                        list.addAll(Arrays.asList(profileProvider.getSupported()));
                    }
                }
                return list.toArray(new String[0]);
            }
    );
    private static final URL DEFAULT_URL;

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    private static final SingletonMap<String, Profile> WRAPPER_PROFILES = SingletonMap.<String, Profile>builder()
            .function(ProfileWrapper::new).build();

    private static final Singleton<Long> RELOAD_INTERVAL_SINGLETON = Singleton.with(
            () -> Config.BASE_SYSTEM_PROPERTIES.getValue(Profile.class.getName() + ".reloadInterval",
                    () -> toLong(System.getProperty("Profile.reloadInterval"), 0L)
            ) * 1000L
    );
    // 单一资源到URL的映射
    private static final SingletonMap<String, URL> PROFILE_URLS = SingletonMap.<String, URL>builder()
            .function(new Function<String, URL>() {
                @Override
                public URL apply(String key) {
                    String[] ex = allSupportedFileExt();
                    for (String s : ex) {
                        if (key.endsWith(s)) {
                            URL x = Common.getResource(key);
                            if (x != null) return x;
                        }
                        URL x = Common.getResource(key + s);
                        if (x != null) return x;
                    }
                    if (log.isInfoEnabled()) {
                        StringBuilder builder = new StringBuilder("Profile ")
                                .append(key).append(" not found.[");
                        for (int i = 0; i < ex.length; i++) {
                            if (i > 0) {
                                builder.append(", ");
                            }
                            builder.append("'").append(ex[i]).append("'");
                        }
                        builder.append("]");
                        log.info(builder.toString());
                    }
                    return DEFAULT_URL;
                }
            })
            .maxAge(RELOAD_INTERVAL_SINGLETON.get())
            .build();

    // 单一URL到Profile的映射
    private static final SingletonMap<URL, Profile> URL_PROFILES_MAP = SingletonMap.<URL, Profile>builder()
            .function(new Function<URL, Profile>() {
                @Override
                public Profile apply(URL key) {
                    if (key == null)
                        throw new NullPointerException("profile url could not be null.");
                    if (DEFAULT_URL.equals(key)) return NULL_PROFILE;
                    ProfileProvider profileProvider = PROFILE_PROVIDER_LOADER.select(key);
                    if (profileProvider == null) {
                        return NULL_PROFILE;
                    } else {
                        return profileProvider.get(key);
                    }
                }
            })
            .maxAge(RELOAD_INTERVAL_SINGLETON.get())
            .build();
    private static final ServiceLoader<ActiveProfilesProvider> ACTIVE_PROFILES_PROVIDER_SERVICE_LOADER =
            new LazyServiceLoader<ActiveProfilesProvider>() {
            };

    static final SingletonMap<String, Profile> PATH_PROFILE_MAP = SingletonMap.<String, Profile>builder()
            .function(Profile::getByPath)
            .maxAge(RELOAD_INTERVAL_SINGLETON.get())
            .build();

    static {
        try {
            DEFAULT_URL = new URL("file:/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static String[] allSupportedFileExt() {
        return ALL_SUPPORTED_FILE_EXT.get();
    }

    /**
     * 根据url获取Profile
     *
     * @param url 资源url
     * @return profile
     */
    public static Profile get(URL url) {
        return URL_PROFILES_MAP.get(url);
    }

    private static List<URL> getExistsUrl(List<URL> activeProfile, URL baseUrl) {
        if (DEFAULT_URL.equals(baseUrl)) return activeProfile;
        List<URL> list = new ArrayList<>(activeProfile);
        list.add(baseUrl);
        return list;

    }

    private static List<String> getActiveProfiles() {
        List<String> activeProfiles = new ArrayList<>();
        ACTIVE_PROFILES_PROVIDER_SERVICE_LOADER
                .sorted()
                .forEach(
                        activeProfilesProvider -> {
                            for (String s : activeProfilesProvider.getActiveProfiles()) {
                                if (!activeProfiles.contains(s)) {
                                    activeProfiles.add(s);
                                }
                            }
                        }
                );
        return Collections.unmodifiableList(activeProfiles);
    }

    private static Profile getByPath(String path) {
        // 根据active.profiles设置包装所有Profile
        // return get(PROFILE_URLS.get(path));

        List<URL> activeProfileUrls = getActiveProfiles().stream()
                .map(ap -> PROFILE_URLS.get(path + "-" + ap))
                .filter(url -> !DEFAULT_URL.equals(url))
                .collect(Collectors.toList());
        URL baseProfileUrl = PROFILE_URLS.get(path);
        List<URL> exists = getExistsUrl(activeProfileUrls, baseProfileUrl);
        switch (exists.size()) {
            case 0:
                return get(DEFAULT_URL);
            case 1:
                return get(exists.get(0));
            default:
                MergedProfile mergedProfile = new MergedProfile();
                exists.forEach(url -> mergedProfile.merge(get(url)));
                return mergedProfile;
        }
    }

    /**
     * 根据path获取Profile
     *
     * @param path path
     * @return profile
     */
    public static Profile get(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        return WRAPPER_PROFILES.get(path);
    }

    public static Profile get(String path1, String path2, String... others) {
        MergedProfile mergedProfile = new MergedProfile().merge(get(path1)).merge(get(path2));
        if (others != null && others.length > 0) {
            for (String path : others) {
                mergedProfile.merge(get(path));
            }
        }
        return mergedProfile;
    }


//    @Deprecated
//    public static Profile getProfile(URL url) {
//        return get(url);
//    }
//
//    @Deprecated
//    public static Profile getProfile(String path) {
//        return get(path);
//    }

    public boolean getBool(String key, boolean v) {
        return toBool(getString(key), v);
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    protected abstract String getStringImpl(String key);

    protected abstract boolean isNull(String key);

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

    public String getString(String key, Supplier<String> supplier) {
        String s = getStringImpl(key);
        return s == null ? supplier.get() : actualValue(s);
    }

    public String getString(String key, String v) {
        String s = getStringImpl(key);
        return s == null ? v : actualValue(s);
    }


    public String getString(String key) {
        return getString(key, (String) null);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    @SuppressWarnings("unused")
    public int getInt(String key, Supplier<Integer> v) {
        return toInt(getString(key), v);
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

    @SuppressWarnings("unused")
    public long getLong(String key, Supplier<Long> v) {
        return toLong(getString(key), v);
    }

    public String[] getStrList(String key) {
        return getStrList(key, ",");
    }

    public String[] getStrList(String key, String delim) {
        return getStrList(key, delim, (String[]) null);
    }

    @SuppressWarnings("unused")
    public String[] getStrList(String key, String delim, Supplier<String[]> supplier) {
        return toArray(getString(key), delim, supplier);
    }

    @SuppressWarnings("unused")
    public String[] getStrList(String key, Supplier<String[]> supplier) {
        return toArray(getString(key), ",", supplier);
    }

    public String[] getStrList(String key, String delim, String[] v) {
        return toArray(getString(key), delim, v);
    }


    private static class NullProfile extends Profile {
        @Override
        protected String getStringImpl(String key) {
            return null;
        }

        @Override
        protected boolean isNull(String key) {
            return true;
        }
    }

}

class ProfileWrapper extends Profile {

    private final String path;

    ProfileWrapper(String path) {
        this.path = path;
    }

    private Profile get() {
        return PATH_PROFILE_MAP.get(path);
    }

    @Override
    protected String getStringImpl(String key) {
        return get().getStringImpl(key);
    }

    @Override
    protected boolean isNull(String key) {
        return get().isNull(key);
    }

    @Override
    public boolean getBool(String key, boolean v) {
        return get().getBool(key, v);
    }

    @Override
    public boolean getBool(String key) {
        return get().getBool(key);
    }

    @Override
    public String getString(String key, String v) {
        return get().getString(key, v);
    }

    @Override
    public String getString(String key) {
        return get().getString(key);
    }

    @Override
    public int getInt(String key) {
        return get().getInt(key);
    }

    @Override
    public int getInt(String key, int v) {
        return get().getInt(key, v);
    }

    @Override
    public long getLong(String key) {
        return get().getLong(key);
    }

    @Override
    public long getLong(String key, long v) {
        return get().getLong(key, v);
    }

    @Override
    public String[] getStrList(String key) {
        return get().getStrList(key);
    }

    @Override
    public String[] getStrList(String key, String delim) {
        return get().getStrList(key, delim);
    }

    @Override
    public String[] getStrList(String key, String delim, String[] v) {
        return get().getStrList(key, delim, v);
    }
}

class MergedProfile extends Profile {
    private final List<Profile> profiles = new ArrayList<>();

    MergedProfile() {
    }

    MergedProfile merge(Profile profile) {
        if (profile != null && !profiles.contains(profile)) {
            if (profile instanceof MergedProfile) {
                for (Profile p : ((MergedProfile) profile).profiles) {
                    merge(p);
                }
            } else {
                profiles.add(profile);
            }
        }
        return this;
    }

    @Deprecated
    MergedProfile merge(String name) {
        if (name == null) return this;
        return merge(Profile.get(name));
    }

    private Profile getFirst(String key) {
        for (Profile p : profiles) {
            if (!p.isNull(key)) return p;
        }
        return NULL_PROFILE;
    }

    @Override
    public boolean getBool(String key, boolean v) {
        return getFirst(key).getBool(key, v);
    }

    @Override
    public boolean getBool(String key) {
        return getFirst(key).getBool(key);
    }

    @Override
    public String getString(String key, String v) {
        return getFirst(key).getString(key, v);
    }

    @Override
    public String getString(String key) {
        return getFirst(key).getString(key);
    }

    @Override
    public int getInt(String key) {
        return getFirst(key).getInt(key);
    }

    @Override
    public int getInt(String key, int v) {
        return getFirst(key).getInt(key, v);
    }

    @Override
    public long getLong(String key) {
        return getFirst(key).getLong(key);
    }

    @Override
    public long getLong(String key, long v) {
        return getFirst(key).getLong(key, v);
    }

    @Override
    public String[] getStrList(String key) {
        return getFirst(key).getStrList(key);
    }

    @Override
    public String[] getStrList(String key, String delim) {
        return getFirst(key).getStrList(key, delim);
    }

    @Override
    public String[] getStrList(String key, String delim, String[] v) {
        return getFirst(key).getStrList(key, delim, v);
    }

    @Override
    protected String getStringImpl(String key) {
        return getFirst(key).getStringImpl(key);
    }

    @Override
    protected boolean isNull(String key) {
        for (Profile p : profiles) {
            if (!p.isNull(key)) return false;
        }
        return true;
    }
}