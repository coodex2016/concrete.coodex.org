/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.concrete.core.intercept.timecheckers;

import org.coodex.concrete.api.ServiceTimingChecker;
import org.coodex.util.Common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * <pre>工作日提供服务
 * 属性:
 *     weekday: 每周哪几天是工作日，0-6分别代表周日-周六，默认为1,2,3,4,5
 *     restDay: 哪些天是休息日，格式yyyy-MM-dd，多个休息日使用“,”分隔，属性名中D大写
 *     workday: 哪些天是工作日，格式yyyy-MM-dd，多个工作日使用“,”分隔
 * <br/>验证优先级
 *     in workday: true
 *     in restDay: false
 *     return is weekday
 * </pre>
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
public class ByWorkDay implements ServiceTimingChecker {

    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private String weekday = "1,2,3,4,5";

    private String restDay = null;

    private String workday = null;

    private boolean inWorkday(String now) {
        return inList(now, workday);
    }

    private boolean inList(String now, String dayList) {
        if (Common.isBlank(dayList)) return false;

        StringTokenizer st = new StringTokenizer(dayList, ",");
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (Common.isBlank(s)) continue;
            if (s.trim().equals(now)) return true;
        }

        return false;
    }

    private boolean inRestDay(String now) {
        return inList(now, restDay);
    }

    private boolean isWeekday() {
        if (Common.isBlank(weekday)) return false;
        StringTokenizer st = new StringTokenizer(weekday, ",");
        boolean[] weekday = {false, false, false, false, false, false, false};
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (Common.isBlank(s)) continue;
            try {
                int num = Integer.parseInt(s.trim());
                if (num < 7 && num >= 0) {
                    weekday[num] = true;
                }
            } catch (Throwable t) {
            }
        }

        Calendar c = Calendar.getInstance();
        return weekday[c.get(Calendar.DAY_OF_WEEK) - 1];
    }


    @Override
    public boolean isAllowed() {
        String now = format.format(new Date());
        if (inWorkday(now)) return true;
        if (inRestDay(now)) return false;
        return isWeekday();
    }


}
