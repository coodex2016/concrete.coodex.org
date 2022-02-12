<#escape x as x?html> 
# ${module.label}

* **模块名称：** ${module.name}
* **模块定义：** ${module.interfaceClass.name}
<#if module.domain?? >* **角色领域：** ${module.domain.value()}</#if>

${module.description!""}

<#list module.units?sort_by("label") as unit>
## <span id="m${unit_index+1}"><#if unit.deprecated>~~</#if><#if unit.label?length == 0>根接口<#else>${unit.label}</#if><#if unit.deprecated>~~</#if></span>

${unit.description!""}


* **method name:** ${unit.method.name}<#if unit.signable??>
* **sign:** ${tool.formatSignable(unit.signable)}</#if><#assign paramCount=unit.parameters?size>
* **path:** ${module.name}${unit.name}
* **Http Method:** ${unit.invokeType}
* **acl:** <#if unit.accessAllow??><#list unit.roles as role>${role} </#list><#else><#if module.domain?? >${module.domain.value()}.ANY<#else>Anonymous</#if></#if>
* **return:** ${tool.formatTypeStr(unit.genericReturnType, module.interfaceClass)}
* **params:** <#if (paramCount > 0)>

| ParamName | Method | Label | Type                  | Description |
| --------- | -- | ---- | --------------------- | ------------ |<#list unit.parameters as param>
| <#if !param.assembled >-<#else>${param.name}</#if> | <#if !param.pathParam>${unit.invokeType}</#if> | ${param.label} | ${tool.formatTypeStr(param.genericType, module.interfaceClass)} | ${tool.tableSafe(param.description)} |</#list><#else>NONE</#if>

<#if genResultJson?default(true)>
::: details 点击查看示意数据
<#noescape>${tool.mockResult(unit)}</#noescape>
:::
</#if>

</#list>
</#escape>