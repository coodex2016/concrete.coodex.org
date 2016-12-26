<#escape x as x?html> 
# ${type}

| property | type  |
| -------- | ---- |
<#list properties?sort_by("name") as p>| ${p.name} | ${tool.formatPOJOTypeInfo(p.type)} |
</#list>

</#escape>