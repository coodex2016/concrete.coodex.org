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

package test.org.coodex.bean.processors.c;

import org.coodex.util.SelectableServiceLoader;
import org.coodex.util.ServiceLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
public class TestBean {

    @Inject
    private SelectableServiceLoader<String, GenericSelectableService<List<Integer>, String>> genericSelectableServiceSelectableServiceLoader;

    @Inject
    private ServiceLoader<GenericService<Map<String, Object>>> genericServiceServiceLoader;

    @Inject
    private SelectableServiceLoader<String, GenericSelectableServiceX>
            genericSelectableServiceXSelectableServiceLoader;
}
