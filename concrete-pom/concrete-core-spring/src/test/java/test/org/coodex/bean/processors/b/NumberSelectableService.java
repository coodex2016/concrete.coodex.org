/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.bean.processors.b;

import org.coodex.util.SelectableService;

import javax.inject.Named;

public interface NumberSelectableService extends SelectableService<Integer> {
    @Named
    class EvenNumberSelectableService implements NumberSelectableService {
        @Override
        public boolean accept(Integer param) {
            return param != null && param % 2 == 0;
        }
    }

    class OddNumberSelectableService implements NumberSelectableService {
        @Override
        public boolean accept(Integer param) {
            return param != null && param % 2 == 1;
        }
    }

    @Named
    class LongOddNumberSelectableService implements SelectableService<Long>{
        @Override
        public boolean accept(Long param) {
            return true;
        }
    }
}