# coodex-mock-spec

尝试定义一种能够贴近实际情况的模拟数据规范，一次配置，到处使用。

先不多说，由浅入深，一步步走着看。

## 最简单的示例 

```xml
            <!-- mock 规范 --> 
            <dependency>
                <groupId>org.coodex</groupId>
                <artifactId>coodex-mock-spec</artifactId>
                <version>0.3.0-MOCK-TRIAL-SNAPSHOT</version>
            </dependency>

            <!-- mock 实现 -->
            <dependency>
                <groupId>org.coodex</groupId>
                <artifactId>coodex-mock-impl</artifactId>
                <version>0.3.0-MOCK-TRIAL-SNAPSHOT</version>
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
	"floatValue":-0.094385386,
	"integerValue":3,
	"stringValue":"sRC0HpXql"
}
```

这样，这两个值被限定在我们的预期范围里了

- `@Mock.Number`修饰说明
    
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
    1. 浮点类型的，根据跨越的整数单位来确定
    
    最大不超过1000，最小为1
    
    例如：
    
    10,[-1,5],8,(20,30),35
    
    byte,short,int,long及其包装类，以0x开头则表示以16进制解析
 
         
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
	"floatValue":-1.444582,
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

在需要模拟的数据中，可能会出现到自身循环关系的情况，例如，部门有个属性是上级部门，对象是一样一样的，我们可以通过设置循环层数来限定对象深度

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
	"floatValue":-0.678496,
	"integerValue":1,
	"pojo":{
		"booleanValue":false,
		"floatValue":-1.8079671,
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
	"floatValue":1.8952734,
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
    模拟结果，String[0].length == String[1].length

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

把实现放到`org.coodex.util.ServiceLoader`支持的环境中。btw，`org.coodex.util.ServiceLoader`支持Java SPI机制以及BeanProvider机制。

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

来吧，看看Map怎么定制模拟。说到这，忘了，约定大于配置，所有注解都是非必须的，也就是说，没有不加注解一样可以模拟数据，加了以后我们可以把mock做到更贴近实际系统数据，嘿嘿嘿，看出来作用了吗，一会再说？

增加一个Map属性
```java
        @Mock.String(range = {"男","女"})
        @Mock.Number("[60,80]")
        public Map<String, Integer> scores;
```

```json
{
	"booleanValue":true,
	"floatValue":0.60435677,
	"integerValue":0,
	"scores":{
		"男":65,
		"女":62
	},
	"stringValue":"concrete"
}
```

效果有了，不过，这种方式只适用于`key`/`value`类型不同的情况，`coodex-mock`在模拟Map的时候，会有一个优先级和容错程度，最大可能的保障可用性。

正确的使用方式是，使用`@Mock.Key` 和 `@Mock.Value` 进行注入

to be continued...

## enjoy it :)


