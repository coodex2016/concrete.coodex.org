# I18N

  i18n: internationalization，国际化

## usage

```java
  // key 为 a.b.c.d 形式
  I18N.translate(key);

  I18N.translate(key, locale);

  // 使用参数对翻译后的模板进行渲染
  I18N.render(key, parameters);

  I18N.render(key, locale), parameters;
```

> render接口参见 [Renderer](org.coodex.util.Renderer.md)

例如，我们要支持一个`app.title`的国际化，在英文环境为`My Application`，在中文环境显示`我的应用`，那么只需要在资源目录的`i18n`下的任意目录中建一套资源：app.yml,app_en_US.yml，app_zh_CN.yml

> 此案例使用yml文件进行示例，也支持properties文件

### app.yml

```yml
# 默认
app:
  title: My Application
```

### app_en_US.yml

```yml
# 英文环境
app:
  title: My Application
```

### app_zh_CN.yml

```yml
# 中文环境
app:
  title: 我的应用
```

### 代码中使用

```java
    System.out.println(I18N.translate("app.title"));
```

不同的语言运行环境可以获取到不同的值。

## 说明

coodex-utitlites提供了一个实现作为I18N默认行为，基于[Profile](org.coodex.util.Profile.md)的国际化

- i18n资源文件放在resources/i18n下，`ProfileBasedTranslateService`会自动检索，减少配置工作量
  - 资源文件支持yaml(依赖`snakeyaml`)和properties
  - 资源文件命名规范为`namespace`(_language)(_COUNTRY).yml|yaml|properties
    - `namespace`可以是多级，例如`a.b.c`，表明此命名空间下的i18n资源可在此文件中查找，资源内容中应该包含`a.b.c.**`
    - `language`可选，参考`java.util.Locale.getISOLanguages()`，大小写不敏感，推荐小写
    - `country`可选，参考`java.util.Locale.getISOCountries()`, 大小写不敏感，推荐大写
  - 检索优先级
    - `coodex.resource.path`中的资源高于其他
      - 同在`coodex.resource.path`中的资源，按照顺序，越靠前优先级越高
    - 文件系统 高于jar包，方便资源修改
      - 名称不同的，越长越优先（匹配度越高）
      - 名称相同的，目录越深越优先
      - 深度相同的，按包字典序
      - 有language优先
      - 有country优先
      - 按profile支持的文件扩展名顺序

concrete-core中也提供了一个实现，基于jdk的`ResourceBundles`，已不推荐使用，保留对之前版本`concrete`的兼容性。

i18n还提供了一个扩展点`org.coodex.util.DefaultLocaleProvider`，用于获取默认的`Locale`，默认是根据当前运行的语言环境获取`Locale`。`concrete-core`中提供了一个基于`ConcreteService`调用者的语言环境作为默认环境的`DefaultLocaleProvider`，从而达到一套服务对多语言客户端的I18N.
