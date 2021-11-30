/* eslint-disable */
import {argumentsError, execute, overload} from '../concrete'

const module = '${moduleName}'

const ${serviceName} = {
<#list methods as method>
${method.jsdoc}
    '${method.name}': <#if method.overloads?size == 1><#assign overload=method.overloads[0]>function (<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>) {
        return execute(module, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>);
    }<#else>overload(module, {
        <#list method.overloads as overload>'${overload.params?size}': function (<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>) {
            return execute(module, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>);
        }<#if overload_has_next>, </#if></#list>
    })</#if><#if method_has_next>, </#if>
    </#list>
}

export default ${serviceName}


<#--<#if method.overloads.length gt 1>         <#list method.overloads as overload>if (arguments.length === ${overload.paramCount}) {-->
<#--<#if overload.paramCount gt 0>            let <#list overload.params as param>${param} = arguments[${param_index}]<#if param_has_next>, </#if></#list>-->
<#--</#if>            return execute(module, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>)-->
        <#--} else </#list> {-->
            <#--return argumentsError(module)-->
        <#--}-->
