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

package test.org.coodex.intf.pojo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by davidoff shen on 2016-09-03.
 */
public class BV {

    @NotNull(message = "ok cannot be NULL!")
    private String ok;


    @Valid
    @NotNull(message = "bvx cannot be NULL")
    private BV bvx = null;

    public BV(String ok) {
        this(ok, new BV(ok, null));
    }

//    public BV getBvx() {
//        return bvx;
//    }

    BV(String ok, BV bv) {
        this.ok = ok;
        this.bvx = bv;
    }

    public String getOk() {
        return ok;
    }
}
