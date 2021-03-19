# coodex libraries

coodex的一些通用库，从concrete项目中分离成独立项目

## change log

- 缺陷修复：SingletonMap首次创建实例时，maxAge参数不生效的问题
- 文档编写

### 2020-05-08

- 缺陷修复：spring boot maven plugin下搜索资源失败的歪问题
- 优化ResourceScanner的私有方法

### 2020-05-07

- 作废Common.byte2hex系列接口，使用base16Encode系列替代，Common.base16Encode支持按行指定列数
- Common.random系列接口定义为左闭右开，新增Common.randomC系列接口，定义为左闭右闭
- UUIDHelper增加Config配置项`uuid.encoder`，默认`base16`，指使用哪种编码方式对uuid进行编码，已支持`base16`/`bse58`/`base64`/`base64UrlSafe`，可自行实现`org.coodex.util.UUIDHelper.Encoder`放到SPI中


### 2020-05-06

- 增加`org.coodex.id.IDGenerator`，可以指定系统的`IDGeneratorService`来提供一致化的String id
  - 默认使用`org.coodex.id.SnowflakeIdGeneratorService`,参数
    - `snowflake.machineId`，机器号，[0-1023]，默认 -1，使用以下两个参数
    - `snowflake.workerId`，工作机号，[0-31]，默认 -1
    - `snowflake.dataCenterId`，数据中心号，[0-31]，默认 -1
    - 以上三个值均为-1时，使用`0`构建ID发生器
  - 可选`org.coodex.id.UUIDGeneratorService`
- 调整getResource和scanResource的方式，新增`org.coodex.util.ResourceScaner`来进行处理
  - 支持`spring boot maven plugin`打包的资源提取
  - 除`classpath`外，可以使用系统参数`coodex.resource.path`来指定文件系统中的资源路径，相对路径使用系统变量`user.dir`，使用路径分隔符隔开，方便打成单一jar包时使用非包内资源
    - 例如
      - linux: -Dcoodex.resource.path=../config:/etc/myApp/config
      - windows: -Dcoodex.resource.path=..\config;c:\etc\myApp\config
        - windows下，不带盘符时，视为在`user.dir`的逻辑盘下，例如 -Dcoodex.resource.path=\etc\myApp\config ，则指`user.dir`的逻辑盘下的`\etc\myApp\config`路径
  - `org.coodex.util.Common.getResource`接口也支持上述参数
- 增加渲染服务机制`org.coodex.util.RenderService`，根据模板内容进行选择
  - 提供基于`java.text.MessageFormat`的实现
  - 提供基于`FreeMarker`的实现，需要引入`coodex-render-freemarker`包
  - `org.coodex.util.Renderer`对外统一提供渲染接口
  - `org.coodex.util.I18N`增加`render`接口，可基于I18N翻译后的内容进行渲染 
    
### 2020-04-07

- 自[concrete](https://github.com/coodex2016/concrete.coodex.org)([文档](https://concrete.coodex.org))项目分离出来单独立项
- 初始版本0.5.0-SNAPSHOT
- ~~弃用`gitbook`，使用[VuePress](https://vuepress.vuejs.org/)编写文档~~