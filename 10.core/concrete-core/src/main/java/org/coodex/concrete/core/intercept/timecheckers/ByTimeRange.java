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

package org.coodex.concrete.core.intercept.timecheckers;

import org.coodex.concrete.common.ServiceTimingChecker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * <pre>按照时间段提供服务
 * 属性：
 * range: HH:mm-HH:mm;...
 * </pre>
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
public class ByTimeRange implements ServiceTimingChecker {

    private static final DateFormat format = new SimpleDateFormat("HH:mm");

    private String range;


    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    @Override
    public boolean isAllowed() {
        if (range == null) return true;
        String now = format.format(new Date());
        StringTokenizer st = new StringTokenizer(range, ";");
        while (st.hasMoreElements()) {
            String str = st.nextToken().trim();
            int index = str.indexOf('-');
            if (index > 0) {
                String start = str.substring(0, index);
                String end = str.substring(index + 1);
                if (start.compareTo(now) <= 0 && end.compareTo(now) >= 0)
                    return true;
            }
        }

        return false;
    }


}
