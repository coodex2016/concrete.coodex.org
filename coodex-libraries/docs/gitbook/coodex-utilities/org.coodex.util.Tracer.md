# Tracer

调式环境中或生产环境碰到性能问题时，可以通过tracer来输出相关信息。使用自定义属性`-Dorg.coodex.util.Tracer=true`开启

## usage


```java
        System.out.println(
                Tracer.newTracer()
//                        //  org.slf4j.Logger
//                        .logger(log)
//                        // logger will be named after clazz
//                        .logger(String.class)
//                        // logger name
//                        .logger("TEST")
//                        // tracer名
//                        .named("test")
//                        // supplier方式指定tracer名
//                        .named(() -> "test")
                        // 需要跟踪记录的代码段
                        .trace(() -> {
                            try {
                                Tracer.putTrace("hello", "coodex"); // 需要跟踪的信息项

                                Tracer.start("case1");
                                Clock.sleep(1000);
                                Tracer.end("case1");

                                Tracer.start("case2");
                                Clock.sleep(300);
                                Tracer.end("case2");
                                if (new Random().nextBoolean())
                                    throw new RuntimeException("em~~~~");
                                else
                                    return "hello tracer.";
                            } catch (Throwable th) {
                                throw rte(th);
                            }
                        })
        );
```