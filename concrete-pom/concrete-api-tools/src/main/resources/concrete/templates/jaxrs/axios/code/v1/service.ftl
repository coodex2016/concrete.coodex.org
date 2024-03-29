/* eslint-disable */
import {execute<#if overloadUsed?default(false)>, overload</#if><#if grableEnabled?default(true)>, grable</#if>} from '../concrete'

const concreteModuleName = '${moduleName}'

const ${serviceName} = {
<#list methods as method>
${method.jsdoc}
    '${method.name}': <#if method.overloads?size == 1><#assign overload=method.overloads[0]>function (<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>) {
        return <#if grableEnabled?default(true)> grable(concreteModuleName)
            ? execute(concreteModuleName, '${method.serviceId}'<#if overload.params?size lt 2><#list overload.params as param>, ${param}</#list><#else>, {<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>}</#if>)
            : </#if>execute(concreteModuleName, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>);
    }<#else>overload(concreteModuleName, {
        <#list method.overloads as overload>'${overload.params?size}': function (<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>) {
            return <#if grableEnabled?default(true)> grable(concreteModuleName)
                ? execute(concreteModuleName, '${method.serviceId}'<#if overload.params?size lt 2><#list overload.params as param>, ${param}</#list><#else>, {<#list overload.params as param>${param}<#if param_has_next>, </#if></#list>}</#if>)
                : </#if>execute(concreteModuleName, `${overload.url}`, '${overload.resultType}', '${overload.httpMethod}'<#if overload.body??>, ${overload.body}</#if>);
        }<#if overload_has_next>, </#if></#list>
    })</#if><#if method_has_next>, </#if>
    </#list>
}

export default ${serviceName}
