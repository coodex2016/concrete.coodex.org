# 通用单例模式

## org.coodex.util.Singleton&lt;T>

很多场景下，我们需要用到单例模式，大部分情况会采用以下方式管理单例

```java
private static final A a = new A();
```

或者懒加载模式

```java
private static A a;

private static A get(){
    if(a == null){
        synchronized(A.class){
            if(a ==null){
                a = new A();
            }
        }
    }
    return a;
}
```

`coodex-utilities`简化了懒加载模式

```java
private static final Singleton<A> A_SINGLETON = Singleton.with(A::new);
```

使用时

```java
A_SINGLETON.get();
```

当然，这个例子不能说明什么，关键是，通过使用build模式，我们可以在代码中隔离掉构建的具体细节

## org.coodex.util.SingletonMap&lt;K, V>

SingletonMap按照键值对的方式管理单例的实例，每个键的值都维持一个单例，可用作缓存；提供了多个重载的get接口，方便单例对象的构建。对于同一个键，同一时刻最多维持一个实例。

- 创建SingletonMap

```java
    private static final SingletonMap<Integer/*键类型*/, String/*值类型*/> SINGLETON_MAP
            = SingletonMap.<Integer,String>builder()
            // 默认的值构建function
            .function(String::valueOf)
            // 最大缓存时间，单位毫秒，<=0则表示不超期
            .maxAge(0)
            // 当已创建的实例被取出时，是否更新他的激活时间，如果maxAge为非0值，则会从此刻起重新计算生命周期，默认false
            .activeOnGet(false)
            // 当实例挂掉的时候，会触发deathListener
            .deathListener((k,v)->{})
            //map的构建器，默认用ConcurrentHashMap
            .mapSupplier(ConcurrentHashMap::new)
            // 不同的map对null作为键值的处理方式不同，为了适配多种map的实现，可以指定nullKey等同于哪个key
            .nullKey(Integer.MIN_VALUE)
            // 管理值对象生命周期的线程池
            .scheduledExecutorService(ExecutorsHelper.newSingleThreadScheduledExecutor("test"))
            .build();
```

builder的参数均有默认值，只有一个需要注意：function，如果不指定，则调用`get`接口时，需要明确的传入构建Function或者Supplier

```java
    //nullKey
    System.out.println(SINGLETON_MAP.get(null));

    //1秒后失效
    System.out.println(SINGLETON_MAP.get(1,1000));

    //使用非默认function来构建值，使用Function<KEY,VALUE>类似
    System.out.println(SINGLETON_MAP.get(2,()->"hello coodex."));
    // 因为2的值已存在，所以看到的还是hello coodex
    System.out.println(SINGLETON_MAP.get(2,()->"can u see me?"));

    // 使用非默认的deathListener
    System.out.println(SINGLETON_MAP.get(3,500,(i,s)->System.out.println("key: " + i + ", value: " + s )));

    // 其他接口不再一一演示

```

```txt
-2147483648
1
hello coodex.
hello coodex.
3
key: 3, value: 3
```

开启logging的debug，还可以看见以下信息：

```txt
2020-05-11 09:49:01.925 [test-1][org.coodex.util.SingletonMap.lambda$get$2(SingletonMap.java:130)]
[DEBUG] 3 die.

2020-05-11 09:49:02.415 [test-1][org.coodex.util.SingletonMap.lambda$get$2(SingletonMap.java:130)]
[DEBUG] 1 die.
```

<!-- 
同样的，也是通过build模式来构建实例，构建时，可以根据键信息进行构建。

SingletonMap也可以用作缓存，构造SingletonMap时，可以传入单例最大生命周期，当单例实例存活超过此周期时，SingletonMap会把它移除，再次需要获取时，会重新build一个。
-->
