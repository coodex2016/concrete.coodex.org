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

package org.coodex.concrete.accounts;

import org.coodex.concrete.common.DateFormatter;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.TenantBuilderWrapper;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.config.Config;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Clock;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.springframework.data.repository.CrudRepository;

import java.text.DateFormat;

import static org.coodex.concrete.accounts.Constants.ORGANIZATION_PREFIX;
import static org.coodex.concrete.common.AccountsErrorCodes.*;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.util.Common.*;

/**
 * Created by davidoff shen on 2017-05-03.
 */
public class AccountsCommon {

    private final static DateFormatter DEFAULT_DATE_FORMATTER = new DateFormatter() {
//        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public DateFormat getDateFormat() {
            return getSafetyDateFormat(DEFAULT_DATE_FORMAT);
        }

        @Override
        public DateFormat getDateTimeFormat() {
            return getSafetyDateFormat(DEFAULT_DATETIME_FORMAT);
        }
    };

    public static final ServiceLoader<DateFormatter> DATE_FORMATTER_SERVICE_LOADER = new ServiceLoaderImpl<DateFormatter>(DEFAULT_DATE_FORMATTER) {
    };
//    public static final Profile_Deprecated SETTINGS = Profile_Deprecated.getProfile("concrete_accounts.properties");
//    public static final RecursivelyProfile RECURSIVELY_SETTING =
//            new RecursivelyProfile(SETTINGS);

    private final static PasswordGenerator DEFAULT_PASSWORD_GENERATOR = new PasswordGeneratorImpl();
    private static final String TAG_ACCOUNTS_SETTING = "concrete_accounts";


    private static final AcceptableServiceLoader<String, PasswordGenerator> PASSWORD_GENERATORS =
            new AcceptableServiceLoader<
                    String, PasswordGenerator>(DEFAULT_PASSWORD_GENERATOR){};

    public static final String getDefaultPassword() {
        return getEncodedPassword(null);
    }

    public static final String getEncodedPassword(String pwd) {
        return PASSWORD_GENERATORS.getServiceInstance(ORGANIZATION_PREFIX).encode(null);
    }

    public static final String getApplicationName() {
//        return SETTINGS.getString("application.name",
//                ConcreteHelper.getProfile().getString("application.name", "coodex.org"));
        return Config.getValue("application.name", "coodex.org", TAG_ACCOUNTS_SETTING, getAppSet());
    }

    public static final boolean getBool(String key, boolean defaultValue) {
        return Config.getValue(key, defaultValue, TAG_ACCOUNTS_SETTING, getAppSet());
    }

    public static final int getInt(String key, int defaultValue) {
        return Config.getValue(key, defaultValue, TAG_ACCOUNTS_SETTING, getAppSet());
    }

    public static final String getString(String key) {
        return getString(key, null);
    }
    public static final String getString(String key, String defaultValue) {
        return defaultValue == null ?
                Config.get(key, TAG_ACCOUNTS_SETTING, getAppSet()) :
                Config.getValue(key, defaultValue, TAG_ACCOUNTS_SETTING, getAppSet());
    }


    public static String getTenant() {
        return TenantBuilderWrapper.getInstance().getTenant();
    }


    /**
     * 通用业务单元：检查可登录对象的授权码
     *
     * @param authCode
     * @param entity
     * @param <E>
     * @return
     */
    public static <E extends CanLoginEntity> E checkAuthCode(String authCode, E entity) {
        IF.isNull(entity.getAuthCodeKeyActiveTime(), ACCOUNT_INACTIVATED);
        IF.not(TOTPAuthenticator.authenticate(authCode, entity.getAuthCodeKey()), AUTHORIZE_FAILED);
        return entity;
    }

    /**
     * 通用业务单元：更新可登录对象的密码
     *
     * @param entity
     * @param password
     * @param authCode
     * @param repo
     * @param <E>
     */
    public static <E extends CanLoginEntity> void updatePassword(E entity, String password, String authCode, CrudRepository<E, String> repo) {
        checkAuthCode(authCode, entity);
        entity.setPassword(AccountsCommon.getEncodedPassword(password));
        putLoggingData("changePwd", "");
        repo.save(entity);
    }

    /**
     * 通用业务单元：获得授权码绑定描述信息
     *
     * @param entity
     * @param authCode
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getAuthenticatorDesc(CanLoginEntity entity, String authCode) {
        if (entity.getAuthCodeKey() != null && entity.getAuthCodeKeyActiveTime() != null) {
            IF.not(TOTPAuthenticator.authenticate(authCode, entity.getAuthCodeKey()), AUTHORIZE_FAILED);
        }
        String authKey = TOTPAuthenticator.generateAuthKey();
        Token token = TokenWrapper.getInstance();
        token.setAttribute("accounts.temp.authKey." + entity.getId(), authKey);
        token.setAttribute("accounts.temp.authKey.validation." + entity.getId(),
                Long.valueOf(Clock.currentTimeMillis() + 10 * 60 * 1000l));
        return TOTPAuthenticator.build(authKey, AccountsCommon.getApplicationName(), entity.getName());
    }

    /**
     * 通用业务单元：绑定授权码密钥
     *
     * @param entity
     * @param authCode
     * @param repo
     * @param <E>
     */
    public static <E extends CanLoginEntity> void bindAuthKey(E entity, String authCode, CrudRepository<E, String> repo) {
        Token token = TokenWrapper.getInstance();
        Long validation = token.getAttribute("accounts.temp.authKey.validation." + entity.getId(),
                Long.class);
        IF.is(Clock.currentTimeMillis() > validation, AUTH_KEY_FAILURE);
        String authKey = token.getAttribute("accounts.temp.authKey." + entity.getId(),
                String.class);
        token.removeAttribute("accounts.temp.authKey.validation." + entity.getId());
        token.removeAttribute("accounts.temp.authKey." + entity.getId());
        IF.not(TOTPAuthenticator.authenticate(authCode, authKey), AUTHORIZE_FAILED);
        entity.setAuthCodeKey(authKey);
        entity.setAuthCodeKeyActiveTime(Clock.getCalendar());

        putLoggingData("bind", authKey);

        repo.save(entity);
        token.setAccountCredible(true);
    }

    /**
     * 通用业务单元：验证密码
     *
     * @param password
     * @param personEntity
     */
    public static void checkPassword(String password, CanLoginEntity personEntity) {
        IF.not(
                getEncodedPassword(password).equals(personEntity.getPassword()),
                LOGIN_FAILED);
    }

    /**
     * 通用业务单元：重置密码
     *
     * @param entity
     * @param repo
     * @param <E>
     */
    public static <E extends CanLoginEntity> void resetPassword(E entity, CrudRepository<E, String> repo) {
        entity.setPassword(AccountsCommon.PASSWORD_GENERATORS.getServiceInstance(Constants.ORGANIZATION_PREFIX).encode(null));
        repo.save(entity);
        putLoggingData("pwd", "reset to default");
    }

    /**
     * 通用业务单元：重置授权码密钥
     *
     * @param entity
     * @param repo
     * @param <E>
     */
    public static <E extends CanLoginEntity> void resetAuthCode(E entity, CrudRepository<E, String> repo) {
        entity.setAuthCodeKey(null);
        entity.setAuthCodeKeyActiveTime(null);
        repo.save(entity);
        putLoggingData("authCode", "reset");
    }

    public static boolean isCredible(String authCode, CanLoginEntity entity) {
        if (entity.getAuthCodeKey() != null) {
            IF.not(TOTPAuthenticator.authenticate(
                    authCode, entity.getAuthCodeKey()), LOGIN_FAILED);
            return true;
        } else {
            return false;
        }
    }
}
