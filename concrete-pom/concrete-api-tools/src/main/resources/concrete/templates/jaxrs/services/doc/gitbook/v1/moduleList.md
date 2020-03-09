<#escape x as x?html> 
# 模块清单

| 模块名 | description |
| ------ |-------------- |
<#list modules as m>| [${m.label}](modules/${m.interfaceClass.name}.md) | ${tool.tableSafe(m.description)} |
</#list>
 
</#escape>