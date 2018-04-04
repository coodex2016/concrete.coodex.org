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

package org.coodex.concrete.accounts.organization.pojo.full;

import org.coodex.concrete.accounts.organization.pojo.Department;
import org.coodex.concrete.accounts.organization.pojo.Institution;
import org.coodex.concrete.accounts.organization.pojo.Person;
import org.coodex.concrete.accounts.organization.pojo.Position;
import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.mockers.ID;
import org.coodex.concrete.api.pojo.StrID;

import java.util.List;

/**
 * Created by davidoff shen on 2017-05-02.
 */
public class InstitutionFull<
        I extends Institution,
        D extends Department,
        J extends Position,
        P extends Person> {

    private String id;
    private I institution;
    private List<InstitutionFull<I, D, J, P>> institutions;

    private List<DepartmentFull<D, J, P>> departments;
    private List<StrID<J>> positions;
    private List<StrID<P>> persons;

    @Description(name = "单位id")
    @ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Description(name = "单位信息")
    public I getInstitution() {
        return institution;
    }

    public void setInstitution(I institution) {
        this.institution = institution;
    }

    @Description(name = "下属单位信息")
    public List<InstitutionFull<I, D, J, P>> getInstitutions() {
        return institutions;
    }


    public void setInstitutions(List<InstitutionFull<I, D, J, P>> institutions) {
        this.institutions = institutions;
    }

    @Description(name = "下属部门信息")
    public List<DepartmentFull<D, J, P>> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentFull<D, J, P>> departments) {
        this.departments = departments;
    }

    @Description(name = "直属职位")
    public List<StrID<J>> getPositions() {
        return positions;
    }

    public void setPositions(List<StrID<J>> positions) {
        this.positions = positions;
    }

    @Description(name = "直属职位上的人员")
    public List<StrID<P>> getPersons() {
        return persons;
    }

    public void setPersons(List<StrID<P>> persons) {
        this.persons = persons;
    }
}
