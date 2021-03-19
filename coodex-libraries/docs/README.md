# Introduction

`coodex libraries`是一组小工具集，包含有

- `coodex-utilities`
- `coodex-utilities-servlet`
- `shared-cache`
  - `shared-cache-jedis`
  - `shared-cache-memcached`
- `coodex-mock-spec`
- `coodex-mock-impl`
- `coodex-billing`
- `coodex-renderer-freemarker`

以上模块使用相同版本号发布,当前版本为`0.5.0-SNAPSHOT`，在本文档中，统一使用`${coodex.libraries.version}`代替，使用者可自行在`pom.xml`中定义此变量。

使用依赖管理的工程，可以在依赖管理中import构件清单模块:

```xml
    <dependency>
        <groupId>org.coodex</groupId>
        <artifactId>coodex-bom</artifactId>
        <version>${coodex.libraries.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
```
