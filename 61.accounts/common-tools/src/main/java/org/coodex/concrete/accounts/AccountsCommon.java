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

import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Profile;
import org.springframework.data.repository.CrudRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.coodex.concrete.accounts.Constants.ORGANIZATION_PREFIX;
import static org.coodex.concrete.common.AccountsErrorCodes.*;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;

/**
 * Created by davidoff shen on 2017-05-03.
 */
public class AccountsCommon {
    public static final Profile SETTINGS = Profile.getProfile("concrete_accounts.properties");
//    public static final RecursivelyProfile RECURSIVELY_SETTING =
//            new RecursivelyProfile(SETTINGS);

    private final static PasswordGenerator DEFAULT_PASSWORD_GENERATOR = new PasswordGeneratorImpl();

    public static final AcceptableServiceLoader<String, PasswordGenerator> PASSWORD_GENERATORS =
            new AcceptableServiceLoader<String, PasswordGenerator>(new ConcreteServiceLoader<PasswordGenerator>() {
                @Override
                public PasswordGenerator getConcreteDefaultProvider() {
                    return DEFAULT_PASSWORD_GENERATOR;
                }
            });

    private final static DateFormatter DEFAULT_DATE_FORMATTER = new DateFormatter() {
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public DateFormat getDateFormat() {
            return dateFormat;
        }

        @Override
        public DateFormat getDateTimeFormat() {
            return dateTimeFormat;
        }
    };

    public static final ConcreteServiceLoader<DateFormatter> DATE_FORMATTER_SERVICE_LOADER = new ConcreteServiceLoader<DateFormatter>() {
        @Override
        protected DateFormatter getConcreteDefaultProvider() {
            return DEFAULT_DATE_FORMATTER;
        }
    };

    public static final String getDefaultPassword() {
        return getEncodedPassword(null);
    }

    public static final String getEncodedPassword(String pwd) {
        return PASSWORD_GENERATORS.getServiceInstance(ORGANIZATION_PREFIX).encode(null);
    }

    public static final String getApplicationName() {
        return SETTINGS.getString("application.name",
                ConcreteHelper.getProfile().getString("application.name", "coodex.org"));
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
        Assert.isNull(entity.getAuthCodeKeyActiveTime(), ACCOUNT_NOT_ACTIVED);
        Assert.not(TOTPAuthenticator.authenticate(authCode, entity.getAuthCodeKey()), AUTHORIZE_FAILED);
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
    public static String getAuthenticatorDesc(CanLoginEntity entity, String authCode) {
        if (entity.getAuthCodeKey() != null && entity.getAuthCodeKeyActiveTime() != null) {
            Assert.not(TOTPAuthenticator.authenticate(authCode, entity.getAuthCodeKey()), AUTHORIZE_FAILED);
        }
        String authKey = TOTPAuthenticator.generateAuthKey();
        Token token = TokenWrapper.getInstance();
        token.setAttribute("accounts.temp.authKey." + entity.getId(), authKey);
        token.setAttribute("accounts.temp.authKey.validation." + entity.getId(),
                Long.valueOf(System.currentTimeMillis() + 10 * 60 * 1000l));
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
        Long validation = token.getAttribute("accounts.temp.authKey.validation." + entity.getId());
        Assert.is(System.currentTimeMillis() > validation, AUTH_KEY_FAILURE);
        String authKey = token.getAttribute("accounts.temp.authKey." + entity.getId());
        token.removeAttribute("accounts.temp.authKey.validation." + entity.getId());
        token.removeAttribute("accounts.temp.authKey." + entity.getId());
        Assert.not(TOTPAuthenticator.authenticate(authCode, authKey), AUTHORIZE_FAILED);
        entity.setAuthCodeKey(authKey);
        entity.setAuthCodeKeyActiveTime(Calendar.getInstance());

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
        Assert.not(
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
            Assert.not(TOTPAuthenticator.authenticate(
                    authCode, entity.getAuthCodeKey()), LOGIN_FAILED);
            return true;
        } else {
            return false;
        }
    }
}
