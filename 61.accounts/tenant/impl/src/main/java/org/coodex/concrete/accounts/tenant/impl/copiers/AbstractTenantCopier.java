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

package org.coodex.concrete.accounts.tenant.impl.copiers;

import org.coodex.concrete.accounts.tenant.entities.AbstractTenantEntity;
import org.coodex.concrete.accounts.tenant.pojo.Tenant;
import org.coodex.concrete.common.AbstractTwoWayCopier;

import java.util.Calendar;

import static org.coodex.concrete.accounts.AccountsCommon.DATE_FORMATTER_SERVICE_LOADER;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public class AbstractTenantCopier<T extends Tenant, E extends AbstractTenantEntity>
        extends AbstractTwoWayCopier<T, E> {

    protected boolean isInit(E e) {
        return e.isUsing() && e.getValidation() == null;
    }

    protected String calendarToStr(Calendar calendar) {
        return calendar == null ? null :
                DATE_FORMATTER_SERVICE_LOADER.getInstance().getDateFormat().format(calendar.getTime());
    }

    @Override
    public E copyA2B(T t, E e) {
        if (isInit(e)) {
            e.setAppSet(t.getAppSet());
            e.setAccountName(t.getAccountName());
        }
        e.setName(t.getName());
        return e;
    }

    @Override
    public T copyB2A(E e, T t) {
        t.setAccountName(e.getAccountName());
        t.setAppSet(e.getAppSet());
        t.setCreated(calendarToStr(e.getCreated()));
        t.setValidation(calendarToStr(e.getValidation()));
        t.setInit(isInit(e));
        t.setName(e.getName());
        t.setUsing(e.isUsing());
        return t;
    }
}
