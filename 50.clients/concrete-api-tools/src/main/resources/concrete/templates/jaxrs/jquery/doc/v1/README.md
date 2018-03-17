# 【项目】jquery-concrete.js API手册

## 约定

### 获得concrete对象

```javascript
// CommonJS
var concrete = require("jquery-concrete");

// AMD
require(["jquery-concrete"], function(cocnrete) {
   // do something
});

//非模块化，直接使用concrete全局对象
concrete.configure(config);
```

* config对象
```javascript
{
        "root": "", //服务根位置
        "onError": function(code, msg){} //异常处理
}
```



### 获取模块

```javascript
var packageName = "aa.bb.cc.dd";
var serviceName = "ServiceName";
var fullName = packageName + "." + serviceName;

// 服务名全局唯一时
var serviceModule = concrete.module(serviceName);

// 否则
serviceModule = concrete.module(serviceName, packageName);
// or
serviceModule = concrete.module(fullName);

```

### 调用服务

```javascript
serviceModule.someMethod(params).success(function(data){
   // .... 
});
```


## 【项目摘要】

* **项目名称：**
* **接口版本：**
* **接口作者：**
* **更新时间：**


## 【建议罗列变更清单】

