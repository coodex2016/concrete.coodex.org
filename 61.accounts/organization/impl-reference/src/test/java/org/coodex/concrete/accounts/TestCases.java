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

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base32;
import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.accounts.organization.reference.api.*;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.test.ConcreteTestCase;
import org.coodex.pojomocker.MockerFacade;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.accounts.AdministratorFromProfileFactory.ADMINISTRATOR_INFO;

/**
 * Created by davidoff shen on 2017-05-11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test.xml")
@Transactional
public class TestCases extends ConcreteTestCase {

    private final static Logger log = LoggerFactory.getLogger(TestCases.class);


    @Inject
    private InstitutionService institutionService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private InformationService informationService;

    @Inject
    private PositionService positionService;

    @Inject
    private PersonService personService;

    @Inject
    private SelfManagementService selfManagementService;



    @Inject
    private LoginService loginService;

    private Token token = TokenWrapper.getInstance();

    protected String getAuthCode(String authKey) throws InvalidKeyException, NoSuchAlgorithmException {
        return String.format("%06d", TOTPAuthenticator.buildCode(
                new Base32().decode(authKey), Calendar.getInstance().getTimeInMillis()
                        / TimeUnit.SECONDS.toMillis(30)));
    }

    @Test
    public void test() throws InvalidKeyException, NoSuchAlgorithmException {
        administratorLogin();
        // 添加顶级部门
        StrID<Institution> top = institutionService.save(MockerFacade.<Institution>mock(Institution.class), null);
        StrID<Department> department = departmentService.save(MockerFacade.<Department>mock(Department.class), top.getId());
        StrID<Position> job1 = positionService.save(MockerFacade.<Position>mock(Position.class), department.getId());
        positionService.grantTo(job1.getId(), new String[]{AccountManagementRoles.ORGANIZATION_MANAGER});
        StrID<Position> job2 = positionService.save(MockerFacade.<Position>mock(Position.class), top.getId());
        StrID<Person> person = personService.save(MockerFacade.<Person>mock(Person.class),new String[]{job1.getId(), job2.getId()});
        person.getPojo().setIdCardNo("430202197807306015");
        personService.update(person.getId(), person.getPojo());
        log.info("{}", JSON.toJSONString(informationService.get(), true));


        loginService.logout();

        token.renew();
        loginService.login(null, person.getPojo().getIdCardNo(), AccountsCommon.getDefaultPassword(), null);
        Assert.assertFalse(token.isAccountCredible());
        String totp = selfManagementService.authenticatorDesc(null);
        String authKey = getAuthKeyFromTOTP(totp);
        log.info("totp: {}, key: {}", totp, authKey);
        selfManagementService.bindAuthKey(getAuthCode(authKey));
        Assert.assertTrue(token.isAccountCredible());
        String newPassword = "password";
        String newCellPhone = "18660186819";
        String newEmail = "jujus.shen@126.com";
        selfManagementService.updateCellPhone(newCellPhone, getAuthCode(authKey));
        selfManagementService.updateEmail(newEmail, getAuthCode(authKey));
        selfManagementService.updatePassword(newPassword, getAuthCode(authKey));
        loginService.logout();

        token.renew();
        loginService.login(null, newCellPhone, newPassword, getAuthCode(authKey));
        Assert.assertTrue(token.isAccountCredible());
        loginService.logout();

        token.renew();
        loginService.login(null, newEmail, newPassword, getAuthCode(authKey));
        Assert.assertTrue(token.isAccountCredible());
        log.info("allRoles: {}", JSON.toJSONString(selfManagementService.getMyRoles(), true));
        log.info("departments: {}", JSON.toJSONString(selfManagementService.getMyDepartments(), true));
        log.info("institutions: {}", JSON.toJSONString(selfManagementService.getMyInstitutions(), true));
        log.info("positions: {}", JSON.toJSONString(selfManagementService.getMyPositions(), true));
        loginService.logout();

    }

    protected String getAuthKeyFromTOTP(String totp){
        return totp.substring(totp.indexOf("secret=") + 7, totp.indexOf("&issuer="));

    }

    protected void administratorLogin() throws InvalidKeyException, NoSuchAlgorithmException {
        String authCode = getAuthCode(ADMINISTRATOR_INFO.getString("authKey"));
        log.info("adminLogin. authCode[{}]", authCode);
        loginService.administratorLogin(null, AccountsCommon.getDefaultPassword(), authCode);
        Assert.assertTrue(token.isAccountCredible());
    }




}
