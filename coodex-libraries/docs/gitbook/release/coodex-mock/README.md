# coodex-mock-spec

尝试定义一种能够贴近实际情况的模拟数据规范，一次配置，到处使用。

先不多说，由浅入深，一步步走着看。

## 最简单的示例

```xml
            <!-- mock 规范 -->
            <dependency>
                <groupId>org.coodex</groupId>
                <artifactId>coodex-mock-spec</artifactId>
                <version>${coodex.libraries.version}</version>
            </dependency>

            <!-- mock 实现 -->
            <dependency>
                <groupId>org.coodex</groupId>
                <artifactId>coodex-mock-impl</artifactId>
                <version>${coodex.libraries.version}</version>
            </dependency>
```

```java
// 使用默认的配置生成一个随机字符串
Mocker.mock(String.class);
```

结果实例：`nMNGvUiJk`

好像没什么鸟用啊。这不是`coodex-mock`的推荐用法，下面我们一点点的来使用mock规范。

## pojo

### 定义一个pojo

```java
    class Pojo{
        // 为了简化代码，我们使用公有属性
        public String stringValue;
        public Integer integerValue;
        public Float floatValue;
        public Boolean booleanValue;
    }
```

```java
        System.out.println(
                JSON.toJSONString(
                        Mocker.mock(Pojo.class),
                        SerializerFeature.PrettyFormat
                )
        );
```

```json
{
	"booleanValue":false,
	"floatValue":-1.0027583E38,
	"integerValue":-815963263,
	"stringValue":"fbtxY4"
}
```

这数据确实是随机生成了，可还是没什么鸟用，怎么办？

### 数字模拟的定义

我们预期`floatValue`的模拟范围是从 `-2.0f` 到 `2.0f`，`integerValue`的模拟范围是 `0`到`4`以及`9`

```java
        @Mock.Number("[0,4],9")
        public Integer integerValue;
        @Mock.Number("[-2.0f, 2.0f]")
        public Float floatValue;
```

再run一下

```json
{
	"booleanValue":false,
	"floatValue":0.84,
	"integerValue":3,
	"stringValue":"sRC0HpXql"
}
```

这样，这两个值被限定在我们的预期范围里了

- `@Mock.Number`修饰说明

  - value（）

    指定模拟范围，不指定则为该类型数据得全域模拟

    范围包括两种：连续范围，单值范围

    连续范围规则如下
    - '['  - 表示一个连续范围开始，且包含此值，float double及其包装类无效
    - '('  - 表示一个连续范围开始，不包含此值
    - ']'  - 表示一个连续范围结束，且包含此值，float double及其包装类无效
    - ')'  - 表示一个连续范围结束，不包含此值

    连续范围的起止值使用 ',' 分隔

    例如 (-100.0f, 200.5f]

    单值范围直接用数值描述

    多个单值范围或连续范围使用 ',' 分割

    特别的，MIN代表该类型的最小值，MAX代表该类型的最大值，不区分大小写，例如[min,0),MAX,15

    各个范围不需要有序，各自模拟的权重，单值为1，连续范围依据：

    1. 整数类型的，此连续范围内整数的个数来确定
    2. 浮点类型的，根据跨越的整数单位来确定

    最大不超过1000，最小为1

    例如：

    10,[-1,5],8,(20,30),35

    byte,short,int,long及其包装类，以0x开头则表示以16进制解析

  - digits():

    小数点后面的位数，对不需要用科学计数法的double/float及其包装类有效，负数表示不用处理，默认为2

### String模拟的定义

我们对stringValue的预期是以下内容中的一个：

- coodex
- concrete
- 真棒

```java
        @Mock.String(range = {"coodex", "concrete", "真棒!"})
        public String stringValue;
```

```json
{
	"booleanValue":true,
	"floatValue":-1.44,
	"integerValue":0,
	"stringValue":"真棒!"
}
```

- `@Mock.String`用法

    模拟配置优先级：

  - txtResource() 存在且有内容时，在资源文件行中模拟
  - range() 非0长字符串，在range范围内模拟
  - charCodeSet() 非0元素宿数组时，结合minLength(),maxLength()模拟
  - 默认，'0'-'9','A'-'Z','a'-'z'范围内，结合minLength(),maxLength()模拟

### String模拟的定义

我们对stringValue的预期是以下内容中的一个：

- coodex
- concrete
- 真棒

```java
        @Mock.String(range = {"coodex", "concrete", "真棒!"})
        public String stringValue;
```

```json
{
	"booleanValue":true,
	"floatValue":-1.44,
	"integerValue":0,
	"stringValue":"真棒!"
}
```

[点我](string.md)看@Mock.String的说明

### `@Mock.Nullable`

如果我们需要一个属性有几率返回`null`时，可以使用`@Mock.Nullable`进行修饰，例如

```java
        @Mock.String(range = {"coodex", "concrete", "真棒!"})
        @Mock.Nullable(probability = 0.3d)
        public String stringValue;
```

这样`stringValue`就有30%几率为`null`

### 其他基础类型模拟

- `@Mock.Char`

    支持的类型, char及其包装类，String

    模拟优先级:

  - value() 为非0元素集合，则在集合范围内模拟
  - range() 为有长度的字符串时，在字符串的字符中模拟
  - 默认：'0'-'9','A'-'Z','a'-'z' 中模拟

- `@Mock.Boolean`

    布尔单值模拟器,支持类型:

  - boolean, Boolean: 布尔值 true, false
  - byte, int, short, long及其包装类: 默认true - 1; false - 0，可通过intTrue和intFalse更改
  - char及其包装类: 默认 true - T; false - F，可通过charTrue和charFalse更改
  - String: 默认true - "true"; false - "false"，可通过strTrue, strFalse更改

### 属性类型循环

在需要模拟的数据中，可能会出现到自身循环关系的情况，例如，部门有个属性是上级部门，类型是一样一样的，如果不限制，那就是子子孙孙无穷尽，愚公移山了，我们可以通过设置循环层数来限定对象深度

```java
    @Mock.Depth(2)
    static class Pojo {
        @Mock.String(range = {"coodex", "concrete", "真棒!"})
        @Mock.Nullable(probability = 0.5d)
        public String stringValue;
        @Mock.Number("[0,4],9")
        public Integer integerValue;
        @Mock.Number("[-2.0f, 2.0f]")
        public Float floatValue;
        public Boolean booleanValue;
        @Mock.Number("[0,4],9")
        @Mock.Dimension(size = 5)
        public int[] intArray;

        public Pojo pojo;
    }
```

```json
{
	"booleanValue":false,
	"floatValue":-0.67,
	"integerValue":1,
	"pojo":{
		"booleanValue":false,
		"floatValue":-1.81,
		"integerValue":1,
		"stringValue":"coodex"
	}
}
```

我们可以看到pojo被模拟了两层

- `@Mock.Depth`
  - value(): 相同类型的深度，最小为1

### 集合或数组

#### 集合或数组维度设置

我们为pojo增加一个属性

```java
        @Mock.Number("[0,4],9") // 单值模拟设置
        @Mock.Dimension(size = 5)
        public int[] intArray;
```

```json
{
	"booleanValue":false,
	"floatValue":1.89,
	"intArray":[1,9,4,4,2],
	"integerValue":1,
	"stringValue":"coodex"
}
```

- `@Mock.Dimension`

    用来定义多维（含一维）集合、数组的维度模拟信息，确定各维度的数组大小

  - size(): >0 表示固定值，负数表示允许为空，几率为size%，否则按照random(min, max)，默认0
  - nullProbability(): 为空的几率，默认不为空
  - min(): size <=0 时，模拟此维度大小的下界，默认 1
  - max(): size <=0 时，模拟此维度大小的上界，默认 5
  - ordered(): 仅对Collection Set Map有效，用以说明是否需要保证稳定性，默认为真

`ordered()`属性主要应用于后续序列模拟，到时候再说

- `@Mock.Dimensions`

    定义多维集合、数组各个维度的模拟配置
  - value(): 维度定义数组，当前属性上，多维度集合、数组的大小设置，按value的数组下标+1确定对应维度
  - same(): 相同维度的集合数组是否大小一致，默认一致

    例如：

    ```java
            @Mock.Dimensions(
                value = {  @Mock.Dimension(size=2), @Mock.Dimension(min=3,max=10)) },
                same = true
            )
            String[][][] string3d;
    ```

    模拟结果，string3d[0].length == string3d[1].length，same 为 false 时，则有可能不等

### 序列模拟器

对于集合，经常的，我们的应用系统数据有一定的规律，例如，从某一个时刻开始，每一定频率产生一个值。

下面我们做一个以一个固定频率的时刻序列来演示mock的序列模拟器

先定义一个序列模拟器工厂接口

```java
public interface TimestampSequenceFactory extends SequenceMockerFactory<String> {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Interval {
        /**
         * @see java.util.Calendar
         * @return 单位
         */
        int timeUnit() default Calendar.MINUTE;

        int interval() default 10;
    }

}

```

Interval注解不是必须的，我们用他来演示如何设置序列发生器的参数

实现工厂和SequenceMocker

```java
public class TimestampSequenceFactoryImpl implements TimestampSequenceFactory {

    @Override
    public SequenceMocker<String> newSequenceMocker(Annotation... annotations) {
        Interval interval = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Interval.class)) {
                interval = (Interval) annotation;
                break;
            }
        }
        return new TimestampSequence(interval);
    }

    static class TimestampSequence implements SequenceMocker<String> {
        private final int timeUnit;
        private final int interval;
        private final Calendar timestamp = Calendar.getInstance();

        TimestampSequence(Interval interval) {
            this.timeUnit = interval == null ? Calendar.MINUTE : interval.timeUnit();
            this.interval = interval == null ? 10 : interval.interval();
            // 初始化开始时间
            timestamp.add(Calendar.DATE, -new Random().nextInt(10));
        }

        @Override
        public String next() {
            timestamp.add(timeUnit, interval);// 增加一个时间间隔
            return Common.calendarToStr(timestamp);
        }
    }
}
```

把实现放到[`SPI`](../coodex-utilities/SPI.md)支持的环境中。

下面以SPI为例演示

把实现类放到 `resources/META-INF/services/org.coodex.mock.SequenceMockerFactory` 中

好了，序列模拟器实现好了，开始配置

在之前的pojo上增加一个属性用来存放序列模拟器产生的值

```java
        @Mock.Dimension(size = 20, ordered = true)
        @Mock.Sequence(name = "timestamp", factory = TimestampSequenceFactory.class)
        @Mock.Inject("timestamp")
        @TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)
        public Set<String> timestamp;
```

模拟结果大致为

```json
{
	"booleanValue":false,
	"floatValue":1.9487758,
	"integerValue":2,
	"timestamp":[
	    "2019-07-17 00:15:27","2019-07-17 01:15:27",
	    "2019-07-17 02:15:27","2019-07-17 03:15:27",
	    "2019-07-17 04:15:27","2019-07-17 05:15:27",
	    "2019-07-17 06:15:27","2019-07-17 07:15:27",
	    "2019-07-17 08:15:27","2019-07-17 09:15:27",
	    "2019-07-17 10:15:27","2019-07-17 11:15:27",
	    "2019-07-17 12:15:27","2019-07-17 13:15:27",
	    "2019-07-17 14:15:27","2019-07-17 15:15:27",
	    "2019-07-17 16:15:27","2019-07-17 17:15:27",
	    "2019-07-17 18:15:27","2019-07-17 19:15:27"]
}
```

我们对`timestamp`属性上的注解简单说明一下

- `@Mock.Dimension`,用来说明这个集合的维度信息，此案例中固定20长，`ordered`特别说明一下，对`Map`/`Set`/`Collection`有效，用以保证序列发生器产生的单值顺序不乱，你可以把ordered改为false对比一下
- `@Mock.Sequence`

    定义一个序列发生器

  - name(): 上下文中的名字。

    这一版的`coodex-mock`设计上，引入了依赖注入理念，对于具体需要模拟的地方，指定好名称即可，由外部设置具体实现

  - factory(): 指定序列模拟器工厂类型，当需要用到是，由它负责生成一个SequenceMocker实例。

    在本例中，我们看到，定义一个序列发生器看似很繁琐，要定义一个Factory的接口，然后写一个实现，这是推荐的实践方案，这种做法的好处在于，mock的实现与最终业务系统的实现是无关的，可以最大限度剥离业务系统与mock的环境隔离

- `@TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)`

    这是我们自定义的注解，用来传递参数，本例中，我们把间隔设为1小时

- `@Mock.Inject`

    依赖注入的理念，用来指定当前单值模拟的时候使用哪个模拟器。

    本案例中，@Mock.Sequence 和 @Mock.Inject 都放在了要模拟的属性上，这不是必须的，定义类的（后面还有几种）注解，只需要在被Inject之前定义好就行，而且重名的，会根据上下文就近原则进行注入

### 模拟一个Map

虽然coodex.org不推荐使用Map作为pojo传递数据，但是mock还是支持模拟Map的。

来吧，看看Map怎么定制模拟。说到这，忘了，约定大于配置，所有注解都是非必须的，也就是说，不加注解一样可以模拟数据，加了以后我们可以把mock做到更贴近实际系统数据，嘿嘿嘿，看出来作用了吗，一会再说。

增加一个Map属性

```java
        @Mock.String(range = {"男","女"})
        @Mock.Number("[60,80]")
        public Map<String, Integer> scores;
```

```json
{
	"booleanValue":true,
	"floatValue":0.62,
	"integerValue":0,
	"scores":{
		"男":65,
		"女":62
	},
	"stringValue":"concrete"
}
```

效果有了，不过，这种方式只适用于`key`/`value`类型不同的情况，`coodex-mock`在模拟Map的时候，会有一个优先级和容错程度，最大可能的保障可用性。

正确的使用方式是，使用`@Mock.Key` 和 `@Mock.Value` 进行注入。在注入之前，我们需要定义一个模拟设置

```java
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Mock.Declaration //声明一个模拟配置
    @interface Setting1{
        //方式1
        @Mock.String(range = {"男","女"})
        String testKey() default "";

        //方式2
        Mock.Number testValue() default @Mock.Number;
    }
```

然后把Map属性的注解改为

```java
        @Setting1(testValue = @Mock.Number("[60,80]"))
        @Mock.Key("testKey")
        @Mock.Value("testValue")
        public Map<String, Integer> scores;
```

结果大致如下

```json
{
	"floatValue":-1.76,
	"integerValue":3,
	"scores":{
		"女":64,
		"男":70
	}
}
```

在本例中，我们看到，声明一个模拟配置需要做的是，使用`@Mock.Declaration`装饰一个注解，然后把这个注解用到你的模拟上下文中即可。

定义配置有两种方式，一种是用明确的模拟器配置，方法名就是配置名，由`@Mock.Inject`/`@Mock.Key`/`@Mock.Value`使用

```java
        //方式1
        @Mock.String(range = {"男","女"})
        String testKey() default "";
```

这种方式下，模拟器配置是明确的，是固定的，好处在于可以定义多个模拟配置，对于带泛型的通用pojo有帮助，声明一个可以服务多种场景。

```java
        //方式2
        Mock.Number testValue() default @Mock.Number;
```

这种方式下，可以在放入上下文的时候指定具体模拟配置，一个定义可以重复使用

以上两种方式是可以混用的，例如：

```java
        @Mock.String(range = {"男","女"})
        Mock.Number map() default @Mock.Number;
```

```java
        @Setting1(map = @Mock.Number("[60,80]"))
        @Mock.Key("map")
        @Mock.Value("map")
        public Map<String, Integer> scores;
```

`coodex-mock`会根据多个模拟器配置选择合适的模拟器，优先级上，方式2 高于 方式1，方式1中（直接在属性上声明的也一样），则根据先后顺序

同样的，`@Mock.Key`/`@Mock.Value`也支持序列模拟器，在上下文中有同名的定义配置时，优先就近的序列模拟、然后是单值模拟

例如

```java
        @Mock.Dimension(size = 5)
        @Mock.Sequence(name = "map", factory = TimestampSequenceFactory.class)
        @TimestampSequenceFactory.Interval(interval = 1, timeUnit = Calendar.HOUR)
        @Setting1(map = @Mock.Number("[60,80]"))
        @Mock.Key("map")
        @Mock.Value("map")
        public Map<String, Integer> scores;
```

```json
{
	"floatValue":1.45,
	"integerValue":1,
	"scores":{
		"2019-07-12 10:37:19":77,
		"2019-07-12 11:37:19":60,
		"2019-07-12 12:37:19":68,
		"2019-07-12 13:37:19":62,
		"2019-07-12 14:37:19":79
	},
	"stringValue":"concrete"
}
```

### 属性关联

pojo的属性之间，通常会有一定的联系，为了更贴近真实数据，`coodex-mock`支持属性关联，我们来走一个例子。

定义一个pojo，x1,x2是加数，sum是和，我们要达到的模拟效果是，sum = x1 + x2

```java
    static class PojoAdd{
        @Mock.Number("[0, 100)")
        public int x1;
        @Mock.Number("[0, 100)")
        public int x2;

        @Mock.Relation(dependencies = {"x1", "x2"}, strategy = "add")
        public int sum;
    }
```

注意`sum`属性的上指定了关联关系，依赖`x1`和`x2`，使用名称为`add`的策略。那这个策略在哪呢？我们往下看，实现这个策略并放到SPI中

```java
import org.coodex.mock.AbstractRelationStrategy;
import org.coodex.util.Parameter;

public class RelationExample extends AbstractRelationStrategy {

    @Strategy("add")
    public int add(
            @Parameter("x1") int x1,
            @Parameter("x2") int x2) {
        return x1 + x2;
    }
}

```

如果你使用`java 8`的`-parameters`编译，那么`@Parameter`也不用加

定义一个公用方法，声明它是`add`依赖策略的算法，参数上，定义好是哪个属性。

```json
{
	"sum":75,
	"x1":32,
	"x2":43
}
```

## 扩展

以上，单值、集合、序列的基本使用都涉及到了。那么问题来了，这些基本的能达到一定的效果，但是还有很多做不到的，怎么办？我们来自定义一个模拟中文姓名的

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Mock
public @interface FullName {
}
```

实现就不贴了，[点我](https://github.com/coodex2016/concrete.coodex.org/blob/030-mock-refactoring/00.coodexCommon/coodex-mock-impl/src/main/java/org/coodex/mock/ext/FullNameTypeMocker.java)查看

`coodex-mock`除了定义规范以外，还根据历史经验，`org.coodex.mock.ext`下提供了一些模拟器

- `@DateTime`， 时间戳模拟配置，支持`java.util.Date`/ `java.util.Calendar`/ `String`
- `@EMail`, 电子邮件模拟配置，支持`String`
- `@FullName`，中文姓名模拟，支持`String`
- `@IdCard`，身份证模拟，支持`String`
- `@IpAddress`, IP地址模拟，支持`String`/ `int[]`/ `Integer[]`/ `byte[]`/ `Byte[]`
- `@VehicleNum`, 车牌号模拟，支持`String`
- `@Coordinates`，经纬度模拟，支持`float[]`/ `Float[]`/ `double[]`/ `Double[]`
- `@MobilePhoneNum`，手机号模拟，支持`String`

## 应用场景

1. 所有支持`AOP` 拦截器的传输POJO的场景下，前后端分离并行开发

    - [`concrete`](https://concrete.coodex.org/)

        mock重构后的版本新增了`concrete-core-mock`模块，推荐的实践方案是，在原来发布服务的模块里，将其依赖进来，注意，使用`test`作用域，然后在`test`作用域的代码里随便建个`class`，`main`方法里写上`SpringApplication.run(YourStarter.class, args)`即可，巨省事

    - `Spring MVC`

        [点我查看示例](https://github.com/coodex2016/concrete.coodex.org/blob/0.4.x/00.coodexCommon/coodex-mock-impl/src/main/java/org/coodex/mock/spring/webmvc/SpringWebMockAspect.java)

1. 文档化

## 配置无配置的pojo

我们的系统通常会用到一些第三方的pojo数据结构，它们可不知道`coodex-mock`，怎么配置这些数据的mock呢？

我们假设Pojo3rd就是一个第三方的数据，并且需要在我们的服务中用到

```java
    interface Pojo3rd{

        String getVehicleNum();

    }
```

我们看看怎么不改它代码的情况下进行配置

### 方案一

在`mock.assign`包下定义一个同结构的pojo，并在其属性上进行配置，例如：

```java
package mock.assign.example;

import org.coodex.mock.Mock;
import org.coodex.mock.ext.VehicleNum;
import test.org.coodex.mock.impl.MockerTest;

@Mock.Assignation(MockerTest.Pojo3rd.class)//指定给谁配置
public class Pojo3rdCase1 {

    @VehicleNum
    public String vehicleNum;
}
```

`codoex-mock`规范定义了`MockerProvider`的实现必须检查`mock.assign`包下所有带有`@Mock.Assignation`的类，将这些配置信息带入到模拟上下文中

```json
{
	"vehicleNum":"川Q52447"
}
```

### 方案二

使用注解定义，并放到上下文里

```java
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Mock.Assignation(Pojo3rd.class)//指定给谁配置
    @interface Pojo3rdCase2{
        // 方法名对应pojo属性名
        // 和@Mock.Declaration一样，两种模式，自行选择
        MobilePhoneNum vehicleNum() default @MobilePhoneNum;
    }
```

如果直接模拟Pojo3rd的话，我们需要改改入口，把这个注解放进去

先在之前的`Pojo`类上定义`Pojo3rdCase2`，然后

```java
        System.out.println(
                JSON.toJSONString(
                        Mocker.mock(Pojo3rd.class,Pojo.class.getAnnotations()), // <--放到上下文
                        SerializerFeature.PrettyFormat
                )
        );
```

```json
{
	"vehicleNum":"18192536319"
}
```

使用`@Mock.Assignation`的时候，和直接在`Pojo`上配置是一样一样的，也可以定义序列、依赖注入。

`coodex-mock`得益于`coodex-utilities`对泛型的支持，so，请大家放心食用。

## enjoy it :)
