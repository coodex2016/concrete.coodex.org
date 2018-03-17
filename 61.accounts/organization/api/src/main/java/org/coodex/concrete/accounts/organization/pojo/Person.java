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

package org.coodex.concrete.accounts.organization.pojo;

import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.mockers.*;
import org.coodex.concrete.accounts.AbstractPojo;
import org.coodex.concrete.common.RelationPolicies;
import org.coodex.pojomocker.Relation;
import org.coodex.pojomocker.annotations.INTEGER;

/**
 * Created by davidoff shen on 2017-04-28.
 */
public class Person extends AbstractPojo {
    @Description(name = "人员生日",
            description = "格式：yyyy-MM-dd; 如果身份证号已设置则已身份证号的信息为准")
    @DateTime
    @Relation(properties = "idCardNo", policy = RelationPolicies.ID_CARD_TO_BIRTHAY)
    private String birthDay;

    @INTEGER(range = {1, 2})
    @Description(name = "性别",
            description = "参见GB2261：0 - 未知的性别; 1 - 男性; 2 - 女性; " +
                    "5 - 女性改（变）为男性; 6 - 男性改（变）为女性; 9 - 未说明的性别。" +
                    "如果身份证号已设置则以身份证号为准")
    @Relation(properties = "idCardNo", policy = RelationPolicies.ID_CARD_TO_SEX)
    private Integer sex;

    // 可用作登录
    @IdCard
    @Description(name = "身份证号", description = "18位。可用作登录信息")
    private String idCardNo;

    @Description(name = "手机号", description = "11位，可用作登录信息")
    @MobilePhoneNum
    private String cellphone;

    @EMail
    @Description(name = "电子邮件", description = "可用作登录信息")
    private String email;

    @Description(name = "员工号", description = "同一单位内唯一，可与单位信息一起作为登录信息")
    private String code;

    @Description(name = "人员姓名")
    @Override
    @Name
    public String getName() {
        return super.getName();
    }


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


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
