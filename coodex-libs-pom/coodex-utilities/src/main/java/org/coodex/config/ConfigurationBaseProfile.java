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

package org.coodex.config;

import org.coodex.util.Profile;

import java.util.List;

public class ConfigurationBaseProfile extends AbstractConfiguration {

    private static String PROFILES_ROOT = System.getProperty("config.profile.root", "");
    private static String DEFAULT_PROFILE = System.getProperty("config.profile.default", "coodex");

//    public static void main(String[] args) {
//        ConfigurationBaseProfile profile = new ConfigurationBaseProfile();
//        profile.get("key", "a", "b", "c", "d", "e");
//    }

    protected String getDefaultProfile() {
        return DEFAULT_PROFILE;
    }

    @Override
    protected String search(String namespace, List<String> keys) {
        if (namespace == null) namespace = getDefaultProfile();
        Profile profile = Profile.get(PROFILES_ROOT + "/" + namespace /* + ".properties" */);
        for (String k : keys) {
            String x = profile.getString(k);
//            System.out.println(String.format("search %s in: %s", k, namespace));
            if (x != null) return x;
        }
        return null;
    }


//    private String search(String key, List<String> namespaces, int deep) {
//        String profile = "";
//        String searchKey = "";
//        if (namespaces == null) {
//            profile = PROFILES_ROOT + "/" + getDefaultProfile();
//            return Profile.getProfile(profile).getString(key);
//        }
//
//        for (int i = 0; i < namespaces.size(); i++) {
//            String ns = namespaces.get(i);
//
//            if (Common.isBlank(ns)) continue;
//            if (i >= deep) {
//                searchKey += (Common.isBlank(searchKey) ? "" : ".") + ns;
//            } else {
//                profile += (Common.isBlank(profile) ? "" : ".") + ns;
//            }
//        }
//        profile = Common.isBlank(profile) ?
//                PROFILES_ROOT + "/" + getDefaultProfile() :
//                profile + ".properties";
//        searchKey += (Common.isBlank(searchKey) ? "" : ".") + key;
//
//        String x = Profile.getProfile(profile).getString(searchKey);
//        return x != null || deep == 0 ? x : search(key, namespaces, deep - 1);
//    }
}
