
# ${module.label}

## 获得模块
<#assign insName=tool.camelCase(module.interfaceClass.simpleName)>
```javascript
var ${insName} = concrete.module(getName);
```
${module.description!""}

## 接口
<#list module.units as unit>
### <#if unit.deprecated>~~</#if>${unit.method.name}<#if unit.deprecated>~~</#if>

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

| paramName | label | Type                  | description |
| --------- |---- | --------------------- | ------------ |<#list unit.parameters as param>
| ${param.name} | ${param.label} | ${tool.formatTypeStr(param.genericType, module.interfaceClass)} | ${tool.tableSafe(param.description)} |</#list><#else>NONE</#if>
</#escape>
</#list>
