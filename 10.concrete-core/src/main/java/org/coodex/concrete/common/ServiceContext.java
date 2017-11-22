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

package org.coodex.concrete.common;

import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.core.messages.Courier;
import org.coodex.concrete.core.token.TokenWrapper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.SIDE_SERVER;

public abstract class ServiceContext {

    protected Caller caller;
    protected String model;
    protected Integer side = SIDE_SERVER;
    protected Subjoin subjoin = SubjoinWrapper.getInstance();
    protected Token token = TokenWrapper.getInstance();
    protected Map<String, Object> logging = new HashMap<String, Object>();
    protected AbstractUnit currentUnit;
    protected Courier courier;

    public Caller getCaller() {
        return caller;
    }


    public String getModel() {
        return model;
    }


    public Integer getSide() {
        return side;
    }


    public Subjoin getSubjoin() {
        return subjoin;
    }


    public Locale getLocale() {
        return getSubjoin() == null ? Locale.getDefault() : getSubjoin().getLocale();
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Map<String, Object> getLogging() {
        return logging;
    }


    public AbstractUnit getCurrentUnit() {
        return currentUnit;
    }

    public Courier getCourier() {
        return courier;
    }
}
