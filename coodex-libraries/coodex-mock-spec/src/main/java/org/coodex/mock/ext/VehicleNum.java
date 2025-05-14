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

import org.coodex.mock.Mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Mock
public @interface VehicleNum {
    //是否含挂车
    boolean towedVehicle() default false;

    //是否含教练车
    boolean coachVehicle() default false;

    //是否包含政府车
    boolean governmentVehicle() default false;

    Province[] belong() default {
            Province.西藏, Province.浙江, Province.湖北, Province.贵州, Province.广西,
            Province.重庆, Province.青海, Province.山西, Province.安徽, Province.内蒙古,
            Province.陕西, Province.四川, Province.湖南, Province.上海, Province.河北,
            Province.辽宁, Province.天津, Province.新疆, Province.海南, Province.甘肃,
            Province.吉林, Province.福建, Province.河南, Province.云南, Province.黑龙江,
            Province.山东, Province.江苏, Province.江西, Province.宁夏, Province.北京,
            Province.广东};


    @SuppressWarnings("NonAsciiCharacters")
    enum Province {
        西藏('藏', "ABCDEFG"), 浙江('浙', "ABCDEFGHJKL"),
        湖北('鄂', "ABCDEFGHJKLMNPQ"), 贵州('贵', "ABCDEFGHJ"),
        广西('桂', "ABCDEFGHJKMLNP"), 重庆('渝', "ABCFGH"),
        青海('青', "ABCDEFGH"), 山西('晋', "ABCDEFHJKLM"),
        安徽('皖', "ABCDEFGHJKLMNPQR"), 内蒙古('蒙', "ABCDEFGHJKL"),
        陕西('陕', "ABCDEFGHJKU"), 四川('川', "ABCDEFHJKLQRSTUVW"),
        湖南('湘', "ABCDEFGHJKLMNP"), 上海('沪', "ABDC"),
        河北('冀', "ABCDEFGHJRT"), 辽宁('辽', "ABCDEFGHJKLMNPV"),
        天津('津', "ABCE"), 新疆('新', "ABCDEFGHJKLMNPQR"),
        海南('琼', "ABC"), 甘肃('甘', "ABCDEFGHJKLMNP"),
        吉林('吉', "ABCDEFGH"), 福建('闽', "ABCDEFGHJK"),
        河南('豫', "ABCDEFGHJKLMNPQRSU"), 云南('云', "ABCDEFGHJLKMNPQRS"),
        黑龙江('黑', "ABCDEFGHJKLMNP"), 山东('鲁', "ABCDEFGHJKLMNPQRU"),
        江苏('苏', "ABCDEFGHJKLMN"), 江西('赣', "ABCDEFGHJKL"),
        宁夏('宁', "ABCD"), 北京('京', "ACEFGB"),
        广东('粤', "ABCDEFGHJKLMNPQRSTUVWXYZ");

        private final char x;
        private final char[] code;

        Province(char x, String code) {
            this.x = x;
            this.code = code.toUpperCase().toCharArray();
        }

        public char getX() {
            return x;
        }

        public char[] getCode() {
            return code;
        }
    }
}
