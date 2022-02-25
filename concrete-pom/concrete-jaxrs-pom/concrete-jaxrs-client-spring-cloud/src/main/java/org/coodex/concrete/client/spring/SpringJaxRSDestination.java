/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.client.spring;

import org.coodex.concrete.client.jaxrs.JaxRSDestination;

import java.util.Objects;

public class SpringJaxRSDestination extends JaxRSDestination {

    private boolean microService;

    public boolean isMicroService() {
        return microService;
    }

    public void setMicroService(boolean microService) {
        this.microService = microService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpringJaxRSDestination)) return false;
        if (!super.equals(o)) return false;
        SpringJaxRSDestination that = (SpringJaxRSDestination) o;
        return microService == that.microService;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), microService);
    }
}
