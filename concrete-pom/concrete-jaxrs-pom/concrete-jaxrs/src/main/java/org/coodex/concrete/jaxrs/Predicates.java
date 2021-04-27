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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.config.Config;
import org.coodex.util.Common;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.jaxrs.JaxRSHelper.getSubmitBody;


/**
 * Jax RS 谓词定义，尽最大可能贴近real RESTFul风格
 * Created by davidoff shen on 2016-12-01.
 */
public class Predicates {

    public static final String[] HTTP_METHOD = new String[]{
            HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.POST, HttpMethod.GET};
    private static final String TAG_JAXRS_PREDICATES = "jaxrs.predicates";
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
            Config.getArray(
                    "service.predicates.PUT", PREDICATES_PUT_DEFAULT, TAG_JAXRS_PREDICATES, getAppSet());

    /**
     * 使用GET方法的谓词
     * service.predicates.GET
     */
    private static final String[] PREDICATES_GET = Config.getArray(
            "service.predicates.GET", PREDICATES_GET_DEFAULT, TAG_JAXRS_PREDICATES, getAppSet());

    /**
     * 使用DELETE方法的谓词
     * service.predicates.DELETE
     */
    private static final String[] PREDICATES_DELETE =
            Config.getArray(
                    "service.predicates.DELETE", PREDICATES_DELETE_DEFAULT, TAG_JAXRS_PREDICATES, getAppSet());

    /**
     * 使用POST方法的谓词
     * service.predicates.POST
     */
    private static final String[] PREDICATES_POST =
            Config.getArray(
                    "service.predicates.POST", PREDICATES_POST_DEFAULT, TAG_JAXRS_PREDICATES, getAppSet());

    /**
     * @see #PREDICATES_PUT
     * @see #PREDICATES_GET
     * @see #PREDICATES_DELETE
     * @see #PREDICATES_POST
     */
    public static final String[][] PREDICATES = new String[][]{
            PREDICATES_PUT, PREDICATES_DELETE, PREDICATES_POST, PREDICATES_GET};

//    private static Profile_Deprecated getProfile() {
//        return Profile_Deprecated.getProfile("jaxrs.predicates.properties");
//    }

    /**
     * <pre>默认的谓词：
     * update(更新)/set(更新部分)开头，使用PUT
     * new(获取初始值)/get(根据id获取)/find(查询)开头，使用GET
     * del，使用DELETE
     * save, POST
     *
     * 其它：
     * 无参数时用GET
     * 其余POST
     *
     * 可通过jaxrs.predicates.properties重载
     * </pre>
     *
     * @param unit unit
     * @return http方法
     * @see #PREDICATES
     */
    public static String getHttpMethod(JaxrsUnit unit) {
//        String methodName = ConcreteHelper.getMethodName(unit.getMethod());
//        String[] paths = paths(methodName);
//        int index = getLastNodeIndex(paths);
//        if (index >= 0) {
//            String last = paths[index];
        String methodName = unit.getMethod().getName();
        int methodIndex = -1;
        String lastCheck = null;
        for (int i = 0; i < PREDICATES.length; i++) {
            for (int j = 0; j < PREDICATES[i].length; j++) {
                if (methodName.startsWith(PREDICATES[i][j])) {
                    int l = lastCheck == null ? 0 : lastCheck.length();
                    if (PREDICATES[i][j].length() > l) {
                        methodIndex = i;
                        lastCheck = PREDICATES[i][j];
                    }
//                    return HTTP_METHOD[i];
                }
            }
        }
        if (methodIndex >= 0) return HTTP_METHOD[methodIndex];
//        }
//        Annotation[][] annotations = method.getParameterAnnotations();
//        for (int i = 0, j = method.getParameterTypes().length; i < j; i++) {
////        for(Class<?> paramType: method.getParameterTypes()){
//            Class<?> paramType = method.getParameterTypes()[i];
//
//            if (!isPrimitive(paramType)) return HttpMethod.POST;
//            if (annotations != null && annotations[i] != null) {
//                for (Annotation annotation : annotations[i]) {
//                    if (BigString.class.isAssignableFrom(annotation.getClass())) {
//                        return HttpMethod.POST;
//                    }
//                }
//            }
//        }
//
//        return HttpMethod.GET;
        return getSubmitBody(unit) == null ? HttpMethod.GET : HttpMethod.POST;
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

    public static final String removePredicate(String name) {
        for (String[] predicates : PREDICATES) {
            for (String predicate : predicates) {
                if (name.startsWith(predicate)) {
                    return Common.lowerFirstChar(name.substring(predicate.length()));
                }
            }
        }
        return name;
    }

//    /**
//     * 去掉path中的谓词
//     *
//     * @param method
//     * @return
//     */
//    public static final String getRESTFulPath(Method method) {
//        String methodName = ConcreteHelper.getMethodName(method);
//
//        String[] paths = paths(methodName);
//        int index = getLastNodeIndex(paths);
//        if (index >= 0) {
//            String lastNode = paths[index];
//            foreign:
//            for (String[] predicates : PREDICATES) {
//                for (String predicate : predicates) {
//                    if (lastNode.startsWith(predicate)) {
//                        lastNode = lastNode.substring(predicate.length());
//                        break foreign;
//                    }
//                }
//            }
//            paths[index] = lastNode;
//        }
//        methodName = buildPath(paths);
//
//        return Common.isBlank(methodName) ? null : lowerFirstChar(methodName);
//    }

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
