<#escape x as x?html>
# ${tool.getPojoName(type)}

**package**: ${tool.getPojoPackage(type)}

| key                   | value    | label      | desc |
|-----------------------|----------|------------| ---- |
 <#list elements as p> | ${p.key} | ${p.value} | ${p.label} | ${p.desc} |
</#list>

</#escape>