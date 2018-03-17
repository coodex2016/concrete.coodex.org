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

package org.coodex.concrete.accounts.organization.impl.copiers;

import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.util.Common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.coodex.concrete.accounts.AccountsCommon.DATE_FORMATTER_SERVICE_LOADER;

/**
 * Created by davidoff shen on 2017-05-11.
 */
public abstract class PersonCopier<T extends Person, E extends AbstractPersonAccountEntity>
        extends PojoCopier<T, E> {

    private final DateFormat dateFormat_ID = new SimpleDateFormat("yyyyMMdd");

    private void setBirthday(E e, String s) {
        try {
            e.setBirthDay(DATE_FORMATTER_SERVICE_LOADER.getInstance().getDateFormat()
                    .format(dateFormat_ID.parse(s)));
        } catch (Throwable th) {
        }
    }

    private void setSex(E e, char ch) {
        try {
            e.setSex((ch - '0') % 2 == 0 ? 2 : 1);
        } catch (Throwable th) {
        }
    }

    @Override
    public E copyA2B(T t, E e) {
        e.setIdCardNo(t.getIdCardNo());
        e.setBirthDay(t.getBirthDay());
        e.setEmail(t.getEmail());
        e.setCellphone(t.getCellphone());
        e.setSex(t.getSex());
        e.setCode(t.getCode());

        if (!Common.isBlank(t.getIdCardNo())) {
            String idCardNo = t.getIdCardNo();
            switch (idCardNo.length()) {
                case 15:
                    setBirthday(e, "19" + idCardNo.substring(6, 12));
                    setSex(e, idCardNo.charAt(14));
                    break;
                case 18:
                    setBirthday(e, idCardNo.substring(6, 14));
                    setSex(e, idCardNo.charAt(17));
                    break;
            }

        }
        return super.copyA2B(t, e);
    }

    @Override
    public E initB(E o) {
//        o.setAuthCodeKey(TOTPAuthenticator.generateAuthKey());
        o.setPassword(AccountsCommon.getDefaultPassword());
        return o;
    }

    @Override
    public T copyB2A(E e, T t) {
        t.setBirthDay(e.getBirthDay());
        t.setCellphone(e.getCellphone());
        t.setCode(e.getCode());
        t.setEmail(e.getEmail());
        t.setIdCardNo(e.getIdCardNo());
        t.setSex(e.getSex());
        return super.copyB2A(e, t);
    }
}
