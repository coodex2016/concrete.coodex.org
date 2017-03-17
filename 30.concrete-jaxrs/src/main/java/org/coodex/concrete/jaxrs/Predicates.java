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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.ConcreteToolkit;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import javax.ws.rs.HttpMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.coodex.concrete.jaxrs.JaxRSHelper.isPrimitive;
import static org.coodex.concrete.jaxrs.JaxRSHelper.lowerFirstChar;


/**
 * Jax RS 谓词定义，尽最大可能贴近real RESTFul风格
 * Created by davidoff shen on 2016-12-01.
 */
public class Predicates {

    private static Profile getProfile() {
        return Profile.getProfile("jaxrs.predicates.properties");
    }


    /**
     * 【默认】使用PUT方法的谓词
     */
    private static final String[] PREDICATES_PUT_DEFAULT = new String[]{"update", "set"};

    /**
     * 【默认】使用GET方法的谓词
     */
    private static final String[] PREDICATES_GET_DEFAULT = new String[]{"new", "get", "findBy"};

    /**
     * 【默认】使用DELETE方法的谓词
     */
    private static final String[] PREDICATES_DELETE_DEFAULT = new String[]{"delete"};

    /**
     * 【默认】使用POST方法的谓词
     */
    private static final String[] PREDICATES_POST_DEFAULT = new String[]{"save"};


    /**
     * 使用PUT方法的谓词
     * service.predicates.PUT
     */
    private static final String[] PREDICATES_PUT =
            getProfile().getStrList(
                    "service.predicates.PUT", ",", PREDICATES_PUT_DEFAULT);

    /**
     * 使用GET方法的谓词
     * service.predicates.GET
     */
    private static final String[] PREDICATES_GET = getProfile().getStrList(
            "service.predicates.GET", ",", PREDICATES_GET_DEFAULT);

    /**
     * 使用DELETE方法的谓词
     * service.predicates.DELETE
     */
    private static final String[] PREDICATES_DELETE =
            getProfile().getStrList(
                    "service.predicates.DELETE", ",", PREDICATES_DELETE_DEFAULT);

    /**
     * 使用POST方法的谓词
     * service.predicates.POST
     */
    private static final String[] PREDICATES_POST =
            getProfile().getStrList(
                    "service.predicates.POST", ",", PREDICATES_POST_DEFAULT);

    /**
     * @see #PREDICATES_PUT
     * @see #PREDICATES_GET
     * @see #PREDICATES_DELETE
     * @see #PREDICATES_POST
     */
    public static final String[][] PREDICATES = new String[][]{
            PREDICATES_PUT, PREDICATES_GET, PREDICATES_DELETE, PREDICATES_POST};

    public static final String[] HTTP_METHOD = new String[]{
            HttpMethod.PUT, HttpMethod.GET, HttpMethod.DELETE, HttpMethod.POST};

    /**
     * <pre>已明确的谓词：
     * update(更新)/set(更新部分)开头，使用PUT
     * new(获取初始值)/get(根据id获取)/find(查询)开头，使用GET
     * del，使用DELETE
     * save, POST
     *
     * 其它：
     * 无参数时用GET
     * 其余POST</pre>
     *
     * @param method
     * @return
     * @see #PREDICATES
     */
    public static String getHttpMethod(Method method) {
        String methodName = ConcreteToolkit.getMethodName(method);
        String[] paths = paths(methodName);
        int index = getLastNodeIndex(paths);
        if (index >= 0) {
            String last = paths[index];
            for (int i = 0; i < PREDICATES.length; i++) {
                for (int j = 0; j < PREDICATES[i].length; j++) {
                    if (last.startsWith(PREDICATES[i][j]))
                        return HTTP_METHOD[i];
                }
            }
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0, j = method.getParameterTypes().length; i < j; i++) {
//        for(Class<?> paramType: method.getParameterTypes()){
            Class<?> paramType = method.getParameterTypes()[i];

            if (!isPrimitive(paramType)) return HttpMethod.POST;
            if (annotations != null && annotations[i] != null) {
                for (Annotation annotation : annotations[i]) {
                    if (BigString.class.isAssignableFrom(annotation.getClass())) {
                        return HttpMethod.POST;
                    }
                }
            }
        }

        return HttpMethod.GET;
    }

    private static String[] paths(String methodName) {
        StringTokenizer st = new StringTokenizer(methodName, "/\\");
        List<String> cache = new ArrayList<String>();
        while (st.hasMoreElements()) {
            String s = st.nextToken().trim();
            if (Common.isBlank(s)) continue;
            cache.add(s);
        }
        return cache.toArray(new String[0]);
    }

    private static String buildPath(String[] paths) {
        StringBuilder builder = new StringBuilder();
        for (String s : paths) {
            if (!Common.isBlank(s)) {
                if (builder.length() > 0) builder.append('/');
                builder.append(s);
            }
        }
        return builder.toString();
    }

    /**
     * 去掉path中的谓词
     *
     * @param method
     * @return
     */
    public static final String getRESTFulPath(Method method) {
        String methodName = ConcreteToolkit.getMethodName(method);

        String[] paths = paths(methodName);
        int index = getLastNodeIndex(paths);
        if (index >= 0) {
            String lastNode = paths[index];
            foreign:
            for (String[] predicates : PREDICATES) {
                for (String predicate : predicates) {
                    if (lastNode.startsWith(predicate)) {
                        lastNode = lastNode.substring(predicate.length());
                        break foreign;
                    }
                }
            }
            paths[index] = lastNode;
        }
        methodName = buildPath(paths);

        return Common.isBlank(methodName) ? null : lowerFirstChar(methodName);
    }

    private static int getLastNodeIndex(String[] paths) {
        int index = -1;
        for (int i = paths.length - 1; i >= 0; i--) {
            String s = paths[i].trim();
            if (!(s.startsWith("{") && s.endsWith("}"))) {
                index = i;
                break;
            }
        }
        return index;
    }
}
