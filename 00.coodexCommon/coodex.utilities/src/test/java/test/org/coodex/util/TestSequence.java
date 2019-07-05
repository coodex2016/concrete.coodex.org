/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.pojomocker.SequenceGenerator;

public class TestSequence implements SequenceGenerator<String> {
    private String key;

    protected String[] values = new String[]{"a", "b", "c", "d"};
    private int index = 0;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void reset() {
        index = 0;
    }

    @Override
    public String next() {
        try {
            return values[index++];
        }finally {
            if(index >= values.length){
                index = index % values.length;
            }
        }
    }
}
