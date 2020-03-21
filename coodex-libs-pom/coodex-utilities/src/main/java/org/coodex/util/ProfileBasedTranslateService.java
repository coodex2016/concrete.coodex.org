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

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ProfileBasedTranslateService extends AbstractTranslateService {

    private final static Logger log = LoggerFactory.getLogger(ProfileBasedTranslateService.class);


    private List<ResourcesMapper> mappers = new LinkedList<>();

    private SingletonMap<CacheKey, String> translateCache = SingletonMap.<CacheKey, String>builder()
            .function(key -> ProfileBasedTranslateService.this.get(key.key, key.locale)).build();

    ProfileBasedTranslateService() {
        Common.forEach((resource, resourceName) -> mappers.add(new ResourcesMapper(resourceName, resource)),
                (root, resourceName) -> {
                    String[] allSupported = Profile.allSupportedFileExt();
                    for (String ext : allSupported) {
                        if (resourceName.endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                },
                "i18n");
    }

    private static int countOfSlash(String resourceName) {
        int count = 0;
        for (char ch : resourceName.toCharArray()) {
            if (ch == '/') count++;
        }
        return count;
    }

    @Override
    protected String translateIfExits(String key, Locale locale) {
        return translateCache.get(new CacheKey(key, locale));
    }

    private boolean in(String key, String name) {
        if (key.equals(name)) return true;
        return key.startsWith(name + ".");
    }

    private String get(String key, Locale locale) {
        List<ResourcesMapper> list = new LinkedList<>();
        for (ResourcesMapper mapper : mappers) {
            if (mapper.accept(locale) && in(key, mapper.name)) {
                list.add(mapper);
            }
        }
        if (list.size() > 0) {
            ResourcesMapper[] mapperArray = list.toArray(new ResourcesMapper[0]);
            Arrays.sort(mapperArray);
            for (ResourcesMapper mapper : mapperArray) {
                String value = Profile.get(mapper.resource).getString(key);
                if (value != null) {
                    log.debug("{}[{}]:{} load from {}", key, locale, value, mapper.resource.toString());
                    return value;
                }
            }
        }
        log.debug("{} not found.", key);
        return null;
    }

//    private void appendResource(URL resource, String resourceName) {
//        mappers.add(new ResourcesMapper(resourceName, resource));
//    }

    private static class ResourcesMapper implements Comparable<ResourcesMapper> {
        private String name;
        private String path;
        private Boolean isFile;
        //        private Boolean isYaml;
        private String ext;
        private int deep;
        private String language = null;
        private String country = null;
        private URL resource;

        ResourcesMapper(String resourceName, URL resource) {
            this.resource = resource;
            this.isFile = resource.toString().startsWith("file:");
            this.deep = countOfSlash(resourceName);
            int indexEnd = resourceName.lastIndexOf('.');
            int indexStart = resourceName.lastIndexOf('/');
            name = resourceName.substring(indexStart + 1, indexEnd);
            ext = resourceName.substring(indexEnd);
            path = resourceName.substring(0, indexStart);
            if (name.length() > 3 && name.charAt(name.length() - 3) == '_') {
                String test = name.substring(name.length() - 2).toUpperCase();
                if (Common.inArray(test, Locale.getISOCountries())) {
                    country = test;
                    name = name.substring(0, name.length() - 3);
                }
            }
            if (name.length() > 3 && name.charAt(name.length() - 3) == '_') {
                String test = name.substring(name.length() - 2).toLowerCase();
                if (Common.inArray(test, Locale.getISOLanguages())) {
                    language = test;
                    name = name.substring(0, name.length() - 3);
                }
            }
        }


        boolean accept(Locale locale) {
            if (language == null) return true;

            if (country == null && locale.getLanguage().equals(language)) return true;

            return locale.getLanguage().equals(language) && locale.getCountry().equals(country);

        }

        /**
         * 排序规则
         * 文件系统 高于jar包
         * 名称不同的，越长越优先（匹配度越高）
         * 名称相同的，越深越优先
         * 深度相同的，按包字典序
         * 有language优先
         * 有country优先
         * 按profile支持的文件扩展名顺序
         *
         * @param o 被比较的对象
         * @return 参见 {@link Comparable#compareTo(Object)}
         */
        @Override
        public int compareTo(ResourcesMapper o) {
            int x = isFile.compareTo(o.isFile);
            if (x != 0) return -x;
            x = name.length() - o.name.length();
            if (x != 0) return -x;
            x = deep - o.deep;
            if (x != 0) return -x;
            x = path.compareTo(o.path);
            if (x != 0) return x;
            if (language == null && o.language != null) return 1;
            if (language != null && o.language == null) return -1;
            if (country == null && o.country != null) return 1;
            if (country != null && o.country == null) return -1;
            x = Common.indexOf(Profile.allSupportedFileExt(), ext) - Common.indexOf(Profile.allSupportedFileExt(), o.ext);
            if (x != 0) return x;
            x = ext.compareTo(o.ext);
            if (x != 0) return -x;
            return resource.toString().compareTo(o.resource.toString());
        }

    }

    private static class CacheKey {
        private String key;
        private Locale locale;

        CacheKey(String key, Locale locale) {
            this.key = key;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (key != null ? !key.equals(cacheKey.key) : cacheKey.key != null) return false;
            return locale != null ? locale.equals(cacheKey.locale) : cacheKey.locale == null;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (locale != null ? locale.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "key='" + key + '\'' +
                    ", locale=" + locale +
                    '}';
        }
    }

//    public static void main(String[] args) {
//        Integer[] buffer = new Integer[]{6,4,123,872,0};
//        Arrays.sort(buffer);
//        for(Integer x: buffer){
//            System.out.print(x + "  ");
//        }
//    }


}
