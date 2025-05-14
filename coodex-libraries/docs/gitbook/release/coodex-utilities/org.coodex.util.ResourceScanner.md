# 资源扫描器

基于约定大于配置的理念实现的一个用于扫描指定包下相关资源的工具类

系统变量`coodex.resource.path`用来指定外部文件系统扩展路径

## usage

```java
        ResourceScanner
                // 扫描到的资源怎么处理
                .newBuilder((url, string) -> {

                            try (InputStream inputStream = url.openStream()) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                Common.copyStream(inputStream, byteArrayOutputStream);
                                System.out.println(url.toString() + " size: " + byteArrayOutputStream.toByteArray().length);

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                )
                // 根据资源名进行过滤，默认不过滤
//                .filter(resourceName -> true)
                // 是否使用扩展路径，即-Dcoodex.resource.path指定的路径
                .extraPath(true)
                .build()
                // 在某些包下扫描
                .scan("org/**/id","test","i18n");
```
