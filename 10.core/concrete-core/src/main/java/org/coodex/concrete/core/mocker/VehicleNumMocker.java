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

package org.coodex.concrete.core.mocker;

import org.coodex.concrete.api.mockers.VehicleNum;
import org.coodex.pojomocker.AbstractMocker;
import org.coodex.util.Common;

import static org.coodex.util.Common.randomChar;

/**
 * Created by davidoff shen on 2017-05-16.
 */
public class VehicleNumMocker extends AbstractMocker<VehicleNum> {

    private final String alphabets = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private final String numbers = "0123456789";

//    private char randomChar(String s) {
//        return s.charAt(Common.random(s.length() - 1));
//    }

    @Override
    public Object mock(VehicleNum mockAnnotation, Class clazz) {
        if (mockAnnotation.belong().length == 0) return null;

        VehicleNum.Province province = mockAnnotation.belong()[Common.random(mockAnnotation.belong().length - 1)];
        StringBuilder builder = new StringBuilder();
        builder.append(province.getX());
        String codeRange = new String(province.getCode());
        if (mockAnnotation.governmentVehicle())
            codeRange += 'O';
        builder.append(randomChar(codeRange));


        int alhpabetCount = 0;
        for (int i = 1; i < 5; i++) {
            //
            if (alhpabetCount < 2 && Math.random() < 0.1) {
                builder.append(randomChar(alphabets));
                alhpabetCount++;
            } else {
                builder.append(randomChar(numbers));
            }
        }

        String lastRange = numbers + alphabets;
        if (mockAnnotation.coachVehicle())
            lastRange += '学';
        if (mockAnnotation.towedVehicle())
            lastRange += '挂';
        builder.append(randomChar(lastRange));


        return builder.toString();
    }

//    public static void main(String [] args){
//        Profile profile = Profile.getProfile("vehicleNum.properties");
//        StringBuilder enumStr = new StringBuilder();
//        StringBuilder defaultValue = new StringBuilder();
//        for(Object key : profile.getProperties().keySet()){
//            String s = (String) key;
//            String v = profile.getString(s);
//            int index=s.indexOf('（');
//            String getName = s.substring(0, index);
//            char jian = s.charAt(index + 1);
//
//            StringBuilder codes = new StringBuilder();
//            for(char ch : v.toUpperCase().toCharArray()){
//                if(ch >='A' && ch <='Z'){
//                    codes.append(ch);
//                }
//            }
//
//            if(enumStr.length() > 0){
//                enumStr.append(", ");
//                defaultValue.append(", ");
//            }
//            enumStr.append(getName).append("('").append(jian).append("', \"").append(codes).append("\")");
//            defaultValue.append("Province.").append(getName);
//        }
//
//        System.out.println(enumStr.toString());
//        System.out.println(defaultValue.toString());
//    }
}
