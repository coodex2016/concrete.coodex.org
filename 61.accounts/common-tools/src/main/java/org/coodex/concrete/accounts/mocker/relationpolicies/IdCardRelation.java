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

package org.coodex.concrete.accounts.mocker.relationpolicies;

import org.coodex.pojomocker.AbstractRelationPolicy;
import org.coodex.pojomocker.RelationMethod;

import static org.coodex.concrete.common.RelationPolicies.ID_CARD_TO_BIRTHAY;
import static org.coodex.concrete.common.RelationPolicies.ID_CARD_TO_SEX;

/**
 * Created by davidoff shen on 2017-05-17.
 */
public class IdCardRelation extends AbstractRelationPolicy {
    @Override
    public String[] getPolicyNames() {
        return new String[]{ID_CARD_TO_SEX, ID_CARD_TO_BIRTHAY};
    }

    @RelationMethod(ID_CARD_TO_SEX)
    public Integer toSex(String idCardNo){
        if (idCardNo != null) {
            switch (idCardNo.length()) {
                case 15:
                    return (idCardNo.charAt(14) - '0') % 2 == 0 ? 2 : 1;
                case 18:
                    return (idCardNo.charAt(16) - '0') % 2 == 0 ? 2 : 1;
            }
        }
        return null;
    }

    @RelationMethod(ID_CARD_TO_BIRTHAY)
    public String toBirthday(String idCardNo){
        if (idCardNo != null) {
            switch (idCardNo.length()) {
                case 15:
                    return "19" + idCardNo.substring(6, 12);
                case 18:
                    return idCardNo.substring(6, 14);
            }
        }
        return null;
    }

}
