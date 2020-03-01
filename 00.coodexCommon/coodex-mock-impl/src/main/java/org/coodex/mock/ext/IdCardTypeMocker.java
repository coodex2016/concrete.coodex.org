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

package org.coodex.mock.ext;

import org.coodex.mock.AbstractTypeMocker;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IdCardTypeMocker extends AbstractTypeMocker<IdCard> {

    private final static Logger log = LoggerFactory.getLogger(IdCardTypeMocker.class);


    public IdCardTypeMocker() {
        try {
            loadDivisions();
        } catch (Throwable th) {
            log.warn("{}", th.getLocalizedMessage(), th);
            administrative_divisions.add("430202");
        }
    }

    @Override
    protected Class[] getSupportedClasses() {
        return new Class[]{String.class};
    }

    @Override
    protected boolean accept(IdCard annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(IdCard mockAnnotation, Type targetType) {

        int size = mockAnnotation == null ? 18 : mockAnnotation.specification().getSize();

        StringBuilder builder = new StringBuilder(getDivision(mockAnnotation == null ? null : mockAnnotation.divisions()));
        builder.append(size == 15 ? birthDay(mockAnnotation).substring(2) : birthDay(mockAnnotation));
        int sex = mockAnnotation == null ? IdCard.Sex.RANDOM.getSex() : mockAnnotation.sex().getSex();
        int num = Common.random(1, 998);
        if (num % 2 != sex) {
            num++;
        }
        builder.append(String.format("%03d", num));
        String idCard = builder.toString();

        return size == 15 ? idCard : idCard + getVerifyChar(idCard);
    }

    private final List<String> administrative_divisions = new ArrayList<String>();

    public static char getVerifyChar(String idCardNumber) {
        char pszSrc[] = idCardNumber.toCharArray();
        int iS = 0;
        int iW[] = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char szVerCode[] = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        for (int i = 0; i < 17; i++) {
            iS += (pszSrc[i] - '0') * iW[i];
        }
        int iY = iS % 11;
        return szVerCode[iY];
    }

    private String birthDay(IdCard mock) {
        int minAge = mock == null ? 5 : Math.max(5, mock.minAge());
        int maxAge = mock == null ? 90 : Math.max(minAge + 10, Math.min(90, mock.maxAge()));
        int thisYear = Clock.now().get(Calendar.YEAR);
        int year = Common.random(thisYear - maxAge, thisYear - minAge);
        int month = Common.random(1, 12);
        return String.format("%d%02d%02d", year, month, Common.random(1, days(year, month)));
    }

    private int days(int year, int month) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2:
                return year % 4 == 0 ? 29 : 28;
            default:
                return 30;
        }
    }

    private String getDivision(String[] divisions) {
        List<String> range = filter(divisions);
        return range.get(Common.random(range.size() - 1));
    }

    private List<String> filter(String[] divisions) {
        List<String> result = new ArrayList<String>();
        for (String division : administrative_divisions) {
            boolean ok = true;
            if (divisions != null && divisions.length > 0) {
                ok = false;
                for (String rule : divisions) {
                    if (rule != null && rule.length() > 0 && division.startsWith(rule)) {
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) result.add(division);
        }
        if (result.size() == 0) {
            StringBuilder builder = new StringBuilder("无匹配的行政区划: [");
            boolean isFirst = true;
            if (divisions != null && divisions.length > 0) {
                for (String division : divisions) {
                    if (!isFirst) builder.append(", ");
                    builder.append(division);
                    isFirst = false;
                }
            }
            builder.append("].");
            throw new RuntimeException(builder.toString());
        }
        return result;
    }


    private void loadDivisions() throws IOException {
        URL url = Common.getResource("administrativeDivisions.txt", IdCardTypeMocker.class.getClassLoader());
        InputStream is = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 8 && line.charAt(0) == ' ') {
                    administrative_divisions.add(line.trim().substring(0, 6));
                }
            }
        }finally {
            reader.close();
        }
    }
}
