<#escape x as x?html> 
# 模块清单

| 模块名 | serviceName | packageName | description |
| ------ |------------ | -------- | ---- |
<#list modules as m>| [${m.label}](modules/${tool.canonicalName(m.name)}.md) | ${m.interfaceClass.simpleName} | ${m.interfaceClass.package.name} | ${m.description!"　"} |
</#list>
 
</#escape>