<#escape x as x?html>
# ${tool.getPojoName(type)}

**package**: ${tool.getPojoPackage(type)}

| key                                  | value    | label      |
|--------------------------------------|----------|------------|
<#list elements as p>| ${p.key} | ${p.value} | ${p.label} | 
</#list>

</#escape>