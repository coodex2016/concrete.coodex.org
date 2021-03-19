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

package org.coodex.mock;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来修饰定制的模拟器注解，例如：
 * <pre>
 * {@literal @}Mock
 * {@literal @}interface MyMocker{}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Mock {

    /**
     * 当需要注入的模拟器找不到时如何处理
     */
    enum NotFound {
        /**
         * 忽略，使用默认模拟器
         */
        IGNORE,
        /**
         * 警告，通过日志框架输出警告信息，使用默认模拟器
         */
        WARN,
        /**
         * 抛异常
         */
        ERROR
    }


    /**
     * <pre>
     * 用来修饰模拟器声明配置信息，声明模拟器的方式有两种，优先级如下：
     *
     *   如果配置信息的属性的值类型是`@Mock`修饰的，则以此属性名为key，以此属性值声明上下文模拟器，
     *   这种方式主要用于公用类，可以将共用类中明确类型的需要被注入的模拟器逐一定义出来，
     *   开发者根据实际情况指定模拟器即可
     *
     *   属性上有`@Mock`修饰的注解时，则以此属性名为`key`，以此属性上的注解声明上下文模拟器，
     *   适用于共用类中，属性类型不明确的，由开发者在外层根据具体情况指定将用到的模拟器
     *
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Declaration {
    }

    /**
     * 定义一个序列模拟器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @interface Sequence {
        /**
         * @return 上下文中的名字
         */
        java.lang.String name();

        /**
         * @return 模拟器的类型。
         *
         * 为了降低模拟定义对实现的依赖，应该定义接口。
         *
         * 如果是类，MockerProvider应予以警告
         */
        Class<? extends SequenceMockerFactory<?>> factory();
    }

    /**
     * 定义一组序列模拟器
     */
    @SuppressWarnings("unused")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @interface Sequences {
        /**
         * @return 所有可能被下文中用到的序列模拟器
         */
        Sequence[] value();
    }

    /**
     * <pre>
     * 用来修饰属性，注入上下文中的模拟器
     * 重名的模拟器优先级上：
     *     在集合、数组的属性模拟上，上下文中同名的序列模拟器优先级高于其他单值模拟器
     *     多个相同类型（单值并且与当前需要模拟的类型匹配的、序列）模拟器，就近原则，越靠近将要模拟的属性则越优先
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @interface Inject {

        /**
         * @return 注入哪个模拟器
         */
        java.lang.String value();

        /**
         * @return 需要注入的模拟器不存在时如何处理，默认提示警告信息，使用此类型的默认模拟器
         */
        NotFound notFound() default NotFound.WARN;
    }

    /**
     * 用来定义多维（含一维）集合、数组的维度模拟信息，确定各维度的数组大小
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Dimension {
        int MAX_DEFAULT = 5;
        int MIN_DEFAULT = 1;
        int SIZE_DEFAULT = 0;
        boolean ORDERED_DEFAULT = true;
        /**
         * @return >0 表示固定值，否则按照random(min, max)，默认0
         */
        int size() default SIZE_DEFAULT;

        /**
         * @return size <=0 时，模拟此维度大小的下界
         */
        int min() default MIN_DEFAULT;

        /**
         * @return size <=0 时，模拟此维度大小的上界
         */
        int max() default MAX_DEFAULT;

        /**
         * @return 为空的几率，默认不为空
         */
        double nullProbability() default -1d;

        /**
         * @return 仅对Collection Set Map有效，用以说明是否需要保证稳定性，默认为真
         */
        boolean ordered() default ORDERED_DEFAULT;
    }

    /**
     * 定义多维集合、数组各个维度的模拟配置
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Dimensions {

        boolean SAME_DEFAULT = true;

        /**
         * @return 当前属性上，多维度集合、数组的大小设置，按value的数组下标+1确定对应维度
         */
        Dimension[] value();

        /**
         * 相同维度的集合数组是否大小一致，默认一致。
         * <p>
         * 例如：
         * <pre>
         * {@literal @}{@link Dimensions}(
         *      value = { {@literal @}{@link Dimension}(size=2),{@literal @}{@link Dimension}(min=3,max=10)) },
         *      same = true
         * )
         * String[][][] string3d;
         *
         * 模拟结果，string3d[0].length == string3d[1].length
         * <p>
         * same 为 false 时，有可能不等
         * </pre>
         *
         * @return 相同维度的集合数组是否大小一致，默认一致
         */
        boolean same() default SAME_DEFAULT;
    }

    /**
     * 用于修饰Map的键模拟器的注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @interface Key {

        java.lang.String value() default "";

        NotFound notFound() default NotFound.WARN;
    }

    /**
     * 用于修饰Map的值模拟器的注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @interface Value {

        java.lang.String value() default "";

        NotFound notFound() default NotFound.WARN;
    }

    /**
     * 使用指定`json`文件模拟数据，优先级最高
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Designated {
        /**
         * @return 指定`json`文件
         */
        java.lang.String resource();
    }

    /**
     * 用来指定pojo的关联模拟策略，也就是被修饰的属性可以根据所依赖的属性值进行运算，最大可能保障模拟数据的真实性
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Relation {
        java.lang.String[] dependencies();

        java.lang.String strategy();
    }

    /**
     * 用来指定第三方pojo的模拟器设置。
     * <p>
     * 修饰的配置类须放到mock.assign包下
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @interface Assignation {
        Class<?> value();
    }

    /**
     * 相同类型模拟的深度，比如
     * <pre>
     *     class A {
     *         public A a;
     *     }
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @interface Depth {
        int DEFAULT_DEPTH = 3;

        /**
         * @return 相同类型的深度，最小为1
         */
        int value() default DEFAULT_DEPTH;
    }


    /**
     * <pre>
     * 单值模拟时，是否模拟null
     * 对基础类型无效
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Mock
    @interface Nullable {
        /**
         * @return null几率，默认5%
         */
        double probability() default 0.05d;
    }

    /**
     * <pre>
     * 布尔单值模拟器,支持类型:
     * boolean, Boolean: 布尔值 true, false
     * byte, int, short, long及其包装类: 默认true - 1; false - 0，可通过intTrue和intFalse更改
     * char及其包装类: 默认 true - T; false - F，可通过charTrue和charFalse更改
     * String: 默认true - "true"; false - "false"，可通过strTrue, strFalse更改
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Mock
    @interface Boolean {
        /**
         * @return 模拟值为真的百分比，默认50%
         */
        double probabilityOfTrue() default 0.5d;

        int intTrue() default 1;

        int intFalse() default 0;

        char charTrue() default 'T';

        char charFalse() default 'F';

        java.lang.String strTrue() default "true";

        java.lang.String strFalse() default "false";
    }


    /**
     * 数据单值模拟器，支持类型：byte, short, int, long, float, double及其包装类
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Mock
    @interface Number {
        int MAX_WEIGHT = 1000;
        java.lang.String DEFAULT_RANGE = "[min, max]";
        int DEFAULT_DIGITS = 2;
        /**
         * <pre>
         * 指定模拟范围，不指定则为该类型数据得全域模拟
         * 范围包括两种：连续范围，单值范围
         * 连续范围规则如下
         * '['  - 表示一个连续范围开始，且包含此值，float double及其包装类无效
         * '('  - 表示一个连续范围开始，不包含此值
         * ']'  - 表示一个连续范围结束，且包含此值，float double及其包装类无效
         * ')'  - 表示一个连续范围结束，不包含此值
         * 连续范围的起止值使用 ',' 分隔
         *
         * 例如 (-100.0f, 200.5f]
         *
         * 单值范围直接用数值描述
         *
         * 多个单值范围或连续范围使用 ',' 分割
         *
         * 特别的，MIN代表该类型的最小值，MAX代表该类型的最大值，不区分大小写，例如[min,0),MAX,15
         *
         * 各个范围不需要有序，各自模拟的权重，单值为1，连续范围依据：
         *      1、整数类型的，此连续范围内整数的个数来确定
         *      2、浮点类型的，根据跨越的整数单位来确定
         * 最大不超过1000，最小为1
         *
         * 例如：
         * 10,[-1,5],8,(20,30),35
         *
         * byte,short,int,long及其包装类，以0x开头则表示以16进制解析
         *
         * </pre>
         *
         * @return 模拟范围
         */
        java.lang.String value() default "";

        /**
         * @return 小数点后面的位数，对不需要用科学计数法的double/float及其包装类有效，负数表示不用处理，默认为2
         */
        int digits() default DEFAULT_DIGITS;
    }

    /**
     * 模拟字符。
     * <p>
     * 支持的类型, char及其包装类，String
     * <pre>
     * 模拟优先级:
     *  - value() 为非0元素集合，则在集合范围内模拟
     *  - range() 为有长度的字符串时，在字符串的字符中模拟
     *  - 默认：'0'-'9','A'-'Z','a'-'z' 中模拟
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Mock
    @interface Char {
        java.lang.String DEFAULT_CHAR_RANGE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        /**
         * @return 字符编码集范围
         * @see CharCodeSet
         */
        CharCodeSet[] value() default {};

        /**
         * @return 字符可选范围
         */
        java.lang.String range() default "";
    }


    /**
     * <pre>
     * 字符串模拟配置
     *
     * 模拟配置优先级：
     *  - txtResource() 存在且有内容时，在资源文件行中模拟
     *  - range() 非0长字符串，在range范围内模拟
     *  - charCodeSet() 非0元素宿数组时，结合minLength,maxLength模拟
     *  - 默认，'0'-'9','A'-'Z','a'-'z'范围内，结合minLength,maxLength模拟
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Mock
    @interface String {
        int DEFAULT_MIN_LENGTH = 5;
        int DEFAULT_MAX_LENGTH = 10;

        /**
         * @return 最小长度
         */
        int minLength() default DEFAULT_MIN_LENGTH;

        /**
         * @return 最大长度
         */
        int maxLength() default DEFAULT_MAX_LENGTH;

        /**
         * @return 模拟的charCode范围
         */
        CharCodeSet[] charCodeSets() default {};

        /**
         * @return 在指定的内容中模拟
         */
        java.lang.String[] range() default {};

        /**
         * @return 使用资源文件模拟，一行一个
         */
        java.lang.String txtResource() default "";
    }

}
