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

package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.fsm.IdentifiedStateLoader;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

public class DemoIDLoader implements IdentifiedStateLoader<DemoIdState, String> {

    private Map<String, DemoIdState> store = new HashMap<String, DemoIdState>();

    @Override
    public DemoIdState newState() {
        String id = Common.getUUIDStr();
        DemoIdState idState = new DemoIdState();
        idState.setId(id);
        store.put(id, idState);
        return idState;
    }

    @Override
    public DemoIdState getState(String s) {
        return store.get(s);
    }
}
