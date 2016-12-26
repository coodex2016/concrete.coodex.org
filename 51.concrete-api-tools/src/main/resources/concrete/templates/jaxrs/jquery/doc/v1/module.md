
# ${module.label}

## 获得模块
<#assign insName=tool.camelCase(module.interfaceClass.simpleName)>
```javascript
var ${insName} = concrete.module("${module.interfaceClass.name}");
```
${module.description!""}

## 接口
<#list module.units as unit>
### ${unit.method.name}

${unit.description!""}

```javascript
// example
${insName}.${unit.method.name}(${tool.mockParameters(unit, module)}).success(function(data) {
  // do something ..
});
```
<#escape x as x?html> 
<#assign paramCount=unit.parameters?size>
* **return:** ${tool.formatTypeStr(unit.genericReturnType, module.interfaceClass)}
* **params:** <#if (paramCount > 0)>

| paramName | Type                  |
| --------- | --------------------- |<#list unit.parameters as param>
| ${param.name} | ${tool.formatTypeStr(param.genericType, module.interfaceClass)} |</#list><#else>NONE</#if>
</#escape>
</#list>
