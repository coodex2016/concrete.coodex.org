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

package org.coodex.concrete.accounts.tenant.impl;

import org.coodex.commons.jpa.springdata.PageHelper;
import org.coodex.commons.jpa.springdata.SpecCommon;
import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.tenant.api.AbstractTenantManagementService;
import org.coodex.concrete.accounts.tenant.entities.AbstractTenantEntity;
import org.coodex.concrete.accounts.tenant.pojo.Tenant;
import org.coodex.concrete.accounts.tenant.pojo.TenantQuery;
import org.coodex.concrete.accounts.tenant.repositories.AbstractTenantRepo;
import org.coodex.concrete.api.pojo.PageRequest;
import org.coodex.concrete.api.pojo.PageResult;
import org.coodex.concrete.api.pojo.StrID;
import org.coodex.concrete.common.*;
import org.coodex.copier.TwoWayCopier;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.coodex.concrete.common.ConcreteContext.putLoggingData;
import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by davidoff shen on 2017-05-26.
 */
public abstract class AbstractTenantManagementServiceImpl<T extends Tenant, E extends AbstractTenantEntity> implements AbstractTenantManagementService<T> {

    @Inject
    protected AbstractTenantRepo<E> tenantRepo;

    @Inject
    protected TwoWayCopier<T, E> copier;

    protected <O extends Serializable> O deepCopy(O value) {
        try {
            return Common.deepCopy(value);
        } catch (Throwable th) {
            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
    }


    protected E getTenantEntity(String tenant) {
        return IF.isNull(tenantRepo.findFirstByAccountName(tenant), AccountsErrorCodes.TENANT_NOT_EXISTS);
    }

    @Override
    public StrID<T> save(String tenant, T tenantInfo) {
        IF.notNull(tenantRepo.findFirstByAccountName(tenant), AccountsErrorCodes.TENANT_ALREADY_EXISTS);
        tenantInfo.setAccountName(tenant);
        E tenantEntity = tenantRepo.save(copier.copyA2B(tenantInfo));
        putLoggingData("new", tenantEntity);
        return new StrID<T>(tenantEntity.getId(), copier.copyB2A(tenantEntity, tenantInfo));
    }

    @Override
    public void update(String tenant, T tenantInfo) {
        E tenantEntity = getTenantEntity(tenant);
        putLoggingData("old", deepCopy(tenantEntity));
        putLoggingData("new", tenantRepo.save(copier.copyA2B(tenantInfo, tenantEntity)));
    }

    @Override
    public PageResult<T> list(PageRequest<TenantQuery> request) {
        List<Specification<E>> specificationList = getListSpecifications(request.getCondition());
        Page<E> page = specificationList.size() > 0 ?
                tenantRepo.findAll(SpecCommon.and(specificationList), toPageable(request)) :
                tenantRepo.findAll(toPageable(request));
        return PageHelper.copy(page, copier.b2aCopier());
    }

    protected Pageable toPageable(PageRequest request) {
        return of(request.getNum().intValue() - 1, request.getPageSize());
    }


    protected List<Specification<E>> getListSpecifications(TenantQuery query) {
        List<Specification<E>> specificationList = new ArrayList<Specification<E>>();
        if (query != null) {
            if (query.isUsing() != null)
                specificationList.add(SpecCommon.<E, Boolean>equals("using", query.isUsing()));

            if (!Common.isBlank(query.getAccountNameLike()))
                specificationList.add(SpecCommon.<E>like("accountName", query.getAccountNameLike()));

            if (!Common.isBlank(query.getNameLike()))
                specificationList.add(SpecCommon.<E>like("name", query.getNameLike()));

        }
        return specificationList;
    }

    //    @Override
//    public PageResult<StrID<T>> list(SortedPageRequest<T> request) {
//        PageRequest<StrID<T>> pageRequest = new PageRequest<StrID<T>>();
//
//        return null;
//    }

    //    @Description(name = "删除租户",
//            description = "初始状态或有效期超期一定年限以上方可删除，" +
//                    "LOGGING: deleted 所有被删除的实体信息")
    @Override
    public void delete(String tenant) {
        E tenantEntity = getTenantEntity(tenant);
        ConcreteException cannotDelete = new ConcreteException(AccountsErrorCodes.TENANT_CANNOT_DELETE);
        IF.not(tenantEntity.isUsing(), cannotDelete);
        Calendar validation = tenantEntity.getValidation();
        if (validation != null) {
            // TODO 配置化
            validation = getValidation(validation, 2, 3);// 两年
            IF.is(Clock.currentTimeMillis() < validation.getTimeInMillis(), cannotDelete);
        }

        tenantRepo.delete(tenantEntity);
        putLoggingData("deleted", tenantEntity);
    }


    @Override
    public void goDownTo(String tenant, int count, int unit) {
        if (count <= 0) return;
        E tenantEntity = getTenantEntity(tenant);
        Calendar validation = getValidation(tenantEntity.getValidation(), count, unit);
        if (tenantEntity.isUsing()) {
            tenantEntity.setValidation(validation);
        } else {
            tenantEntity.setSurplus(tenantEntity.getSurplus() + validation.getTimeInMillis() - Clock.currentTimeMillis());
        }
        putLoggingData("tenant", tenantRepo.save(tenantEntity));
    }

    protected Calendar getValidation(Calendar validation, int count, int unit) {
        validation = validation == null ? Clock.getCalendar() : (Calendar) validation.clone();
        Calendar result = Clock.getCalendar();
        result.setTimeInMillis(Math.max(Clock.currentTimeMillis(), validation.getTimeInMillis()));
        switch (unit) {
            case 1:
                result.add(Calendar.MONTH, count);
                break;
            case 2:
                result.add(Calendar.MONTH, count * 3);
                break;
            case 3:
                result.add(Calendar.YEAR, count);
                break;
            default:
                result.add(Calendar.DATE, count);
        }
        return result;
    }


    @Override
    public void layUp(String tenant) {
        E tenantEntity = getTenantEntity(tenant);
        Calendar validation = tenantEntity.getValidation();
        long now = Clock.currentTimeMillis();
        IF.not(tenantEntity.isUsing() && validation != null &&
                        validation.getTimeInMillis() >= now,
                AccountsErrorCodes.TENANT_UNAVAILABLE);
        if(validation != null) {
            long remainder = validation.getTimeInMillis() - now;
            tenantEntity.setSurplus(remainder);
            tenantEntity.setValidation(null);
        }
        tenantEntity.setUsing(false);
        putLoggingData("tenant", tenantRepo.save(tenantEntity));
    }

    @Override
    public void desterilize(String tenant) {
        E tenantEntity = getTenantEntity(tenant);
        long now = Clock.currentTimeMillis();
        IF.is(tenantEntity.isUsing(), AccountsErrorCodes.TENANT_IN_USING);
        Calendar validation = Clock.getCalendar();
        validation.setTimeInMillis(now + tenantEntity.getSurplus());
        tenantEntity.setSurplus(0l);
        tenantEntity.setValidation(validation);
        tenantEntity.setUsing(true);
        putLoggingData("tenant", tenantRepo.save(tenantEntity));
    }

    @Override
    public void resetPassword(String tenant) {
        AccountsCommon.resetPassword(getTenantEntity(tenant), tenantRepo);
    }

    @Override
    public void resetAuthCode(String tenant) {
        AccountsCommon.resetAuthCode(getTenantEntity(tenant), tenantRepo);
    }

}
