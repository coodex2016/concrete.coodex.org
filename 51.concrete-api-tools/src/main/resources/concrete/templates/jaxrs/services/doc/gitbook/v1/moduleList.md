<#escape x as x?html> 
# 模块清单

| 模块名 | description |
| ------ |-------------- |
<#list modules as m>| [${m.label}](modules/${tool.canonicalName(m.name)}.md) | ${tool.tableSafe(m.description)} |
</#list>
 
</#escape>