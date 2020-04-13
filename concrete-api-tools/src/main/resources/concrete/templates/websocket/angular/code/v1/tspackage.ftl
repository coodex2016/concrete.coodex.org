<#macro paramList params><#list params as param>${param.name}: ${param.type}<#if param_has_next>, </#if></#list></#macro>
<#macro objList params><#list params as param>${param.name}: ${param.name}<#if param_has_next>, </#if></#list></#macro>
<#macro importsList classes><#list classes?sort as clazz>${clazz}<#if clz_has_next>, </#if></#list></#macro>
<#macro genericList typeVariable><#list typeVariable as clazz>${clazz}<#if clz_has_next>, </#if></#list></#macro>
<#if includeServices??>
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';

import { AbstractConcreteService } from '${contextPath}AbstractConcreteService';
</#if>

<#list imports?sort_by("packageName") as import>
import { <@importsList classes=import.classes/> } from '${contextPath}${import.packageName}';
</#list>
<#--?sort_by("className")-->
<#list classes as clazz>
<#if clazz.classType == 0>
<#assign module = clazz/>

@Injectable()
export class ${module.className} extends AbstractConcreteService {

    constructor () {
        super();
    }

    <#--protected $$belong(): string {-->
        <#--return <#if module.belong??>'${module.belong}'<#else >null</#if>;-->
    <#--}-->

<#list module.methods?sort_by("name") as method>
    public ${method.name}(<@paramList params=method.params/>): Observable<${method.returnType}> {
        return this.send('${method.methodPath}', ${method.body});
    }
</#list>
}
<#else >
<#assign pojo = clazz/>

export class ${pojo.className}<#if pojo.genericParams?size != 0><<@genericList typeVariable=pojo.genericParams/>></#if><#if pojo.superClass??> extends ${pojo.superClass}</#if> {
    <#list pojo.fields?sort_by("name") as field>
    ${field.name}: ${field.type};
    </#list>
}

</#if>
</#list>