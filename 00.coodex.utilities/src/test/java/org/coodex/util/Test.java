package org.coodex.util;

import com.alibaba.fastjson.JSON;
import org.coodex.pojomocker.MockerFacade;

/**
 * Created by davidoff shen on 2016-10-19.
 */
public class Test {
    public static void main(String[] args) {
//        System.out.println(JSON.toJSONString(new PojoInfo(B.class), true));
//        System.out.println(new PojoInfo(B.class).getProperty("x").getAnnotations()[0]);
//        System.out.println(new PojoInfo(B.class).getProperty("xxx2").getType());
//        System.out.println(new PojoInfo(new GenericType<A<List<String>>>(){}.genericType(B.class)).getProperty("xxx2").getType());

        for(int i = 0; i < 10; i ++) {
//            System.out.println(MockerFacade.mock(int.class));
            System.out.print(JSON.toJSONString(MockerFacade.mock(new GenericType<A<Byte>>() {
            }), true));
//            System.out.println();
        }
    }
}


