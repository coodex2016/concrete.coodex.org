# 闭包

闭包可以将数据放入上下文中，无需通过大量的参数调用来传递必须的数据。

闭包由上下文和闭包执行体两部分构成，使用时，大致模式如下

```txt
上下文实例.call(需要放入上下文的数据， 闭包执行体)
```

`coodex-utilites`提供了两种闭包上下文

## org.coodex.closure.StackClosureContext&lt;VariantType>

StackClosureContext是一个基于栈模型的闭包上下文，每一次call，都会把最进的数据入栈，执行完后数据出栈，在闭包执行体中获取上下文数据时，永远时最近的那个。

## org.coodex.closure.MapClosureContext&lt;K, V>

MapClosureContext也是一种StackClosureContext，不同的是，上下文中的数据是一个Map，MapClosureContext为执行体提供了按键获取上下文数据的接口，可以同时把多个数据放入一层闭包环境，并且，MapClosureContext还有继承策略，当前层的Map中会继承上一层闭包环境中的Map，相同键值采用就近原则，近的覆盖远的。

---

> 在`coodex-mock-impl`和`concrete`中，大量使用了闭包，减少了很多复杂度。
