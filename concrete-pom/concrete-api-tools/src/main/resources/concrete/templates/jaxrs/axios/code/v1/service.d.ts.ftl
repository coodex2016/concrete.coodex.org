
${declaredTypes}

declare interface ${serviceName} {
<#list methods as method>
<#list method.lines as line>
    ${line}
</#list>
    ${method.def}
</#list>
}

declare const instance: ${serviceName};
export default instance;