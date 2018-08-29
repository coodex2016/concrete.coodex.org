/* eslint-disable */
import {argumentsError, execute} from '../concrete'

const module = '${moduleName}'

const ${serviceName} = {
<#list methods as method>    ${method.name}: function () {
        <#list method.overloads as overload>if (arguments.length === ${overload.paramCount}) {
<#if overload.paramCount gt 0>            let <#list overload.params as param>${param} = arguments[${param_index}]<#if param_has_next>, </#if></#list>
</#if>            return execute(module, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>)
        } else </#list> {
            return argumentsError(module)
        }
    }<#if method_has_next>, </#if>
    </#list>
}

export default ${serviceName}
