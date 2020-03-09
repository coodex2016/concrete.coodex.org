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

package org.coodex.concrete.accounts.organization.reference.impl.services;

import org.coodex.concrete.accounts.organization.impl.AbstractInstitutionManagementServiceImpl;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.reference.api.InstitutionService;
import org.coodex.concrete.accounts.organization.reference.data.entities.InstitutionEntity;
import org.coodex.concrete.accounts.organization.reference.data.entities.PersonEntity;
import org.coodex.concrete.accounts.organization.reference.data.entities.PositionEntity;
import org.coodex.concrete.api.LogAtomic;
import org.coodex.concrete.api.OperationLog;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Created by davidoff shen on 2017-05-19.
 */
@Transactional
@Named
@LogAtomic
@OperationLog
public class InstitutionServiceImpl extends AbstractInstitutionManagementServiceImpl
        <Institution, InstitutionEntity, PositionEntity, PersonEntity>
        implements InstitutionService {
}
