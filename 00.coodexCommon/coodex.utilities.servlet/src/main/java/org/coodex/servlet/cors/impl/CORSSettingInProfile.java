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

/**
 *
 */
package org.coodex.servlet.cors.impl;

import org.coodex.servlet.cors.CORSSetting;
import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author davidoff
 */
public class CORSSettingInProfile implements CORSSetting {

    private static Logger log = LoggerFactory
            .getLogger(CORSSettingInProfile.class);

    private final Profile profile;

    public CORSSettingInProfile() {
        this("cors_settings.properties");
    }

    public CORSSettingInProfile(String profileName) {
        profile = Profile.get(profileName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#allowOrigin()
     */
    @Override
    public String allowOrigin() {
        return profile.getString(KEY_ALLOW_ORIGIN);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#exposeHeaders()
     */
    @Override
    public String exposeHeaders() {
        return profile.getString(KEY_EXPOSE_HEADERS);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#allowMethod()
     */
    @Override
    public String allowMethod() {
        return profile.getString(KEY_ALLOW_METHOD);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#allowHeaders()
     */
    @Override
    public String allowHeaders() {
        return profile.getString(KEY_ALLOW_HEADERS);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#maxAge()
     */
    @Override
    public Long maxAge() {
        String maxAge = profile.getString(KEY_MAX_AGE);
        Long lMaxAge = null;
        try {
            if (maxAge != null)
                lMaxAge = Long.valueOf(maxAge);
        } catch (Throwable th) {
            log.info("cannot load maxAge, type need: long. {} ",
                    th.getLocalizedMessage(), th);
        }
        return lMaxAge;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.coodex.concrete.sp.cors.CORSSetting#allowCredentials()
     */
    @Override
    public Boolean allowCredentials() {
        String allowCredentials = profile.getString(KEY_ALLOW_CREDENTIALS);
        Boolean blAC = null;
        try {
            if (allowCredentials != null)
                blAC = Boolean.valueOf(allowCredentials);

        } catch (Throwable th) {
            log.info("cannot load allowCredentials, type need: boolean. {} ",
                    th.getLocalizedMessage(), th);
        }

        return blAC;
    }

}
