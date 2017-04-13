<#escape x as x?html> 
# ${type}

| property | label | type  | description |
| -------- | ---- | ---- | ----------- |
<#list properties?sort_by("name") as p>| ${p.name} | ${p.label} | ${tool.formatTypeStr(p.type)} | ${tool.tableSafe(p.description)} | 
</#list>

</#escape>