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

package org.coodex.concrete.accounts.organization.entities;

import org.coodex.concrete.accounts.AuthorizableEntity;
import org.coodex.concrete.accounts.CanLoginEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-05-03.
 */
@MappedSuperclass
public abstract class AbstractPersonAccountEntity<J extends AbstractPositionEntity>
        extends AbstractEntity implements AuthorizableEntity, CanLoginEntity {

    private String birthDay;
    private Integer sex;

    // 可用作登录
    private String idCardNo;
    private String cellphone;
    private String email;
    private String code;

    // 密码散列值
    private String password;
    // 二步验证的key
    private String authCodeKey;

    // 二步验证key的激活时间
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar authCodeKeyActiveTime;

    @ElementCollection
    @CollectionTable(name = "c_person_role", joinColumns = @JoinColumn(name = "person"))
    @Column(name = "role")
    private Set<String> roles;

    @ElementCollection
    @CollectionTable(name = "r_person_position", joinColumns = @JoinColumn(name = "person"))
    @Column(name = "position")
    private Set<J> positions;


    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getAuthCodeKey() {
        return authCodeKey;
    }

    @Override
    public void setAuthCodeKey(String authCodeKey) {
        this.authCodeKey = authCodeKey;
    }

    @Override
    public Calendar getAuthCodeKeyActiveTime() {
        return authCodeKeyActiveTime;
    }

    @Override
    public void setAuthCodeKeyActiveTime(Calendar authCodeKeyActiveTime) {
        this.authCodeKeyActiveTime = authCodeKeyActiveTime;
    }

    public Set<J> getPositions() {
        return positions;
    }

    public void setPositions(Set<J> positions) {
        this.positions = positions;
    }
}
