/* tslint:disable */
<#macro paramList params><#list params as param>${param.name}: ${param.type}<#if param_has_next>, </#if></#list></#macro>
<#macro importsList classes><#list classes?sort as clz>${clz}<#if clz_has_next>, </#if></#list></#macro>
<#macro genericList typeVariable><#list typeVariable as clz>${clz}<#if clz_has_next>, </#if></#list></#macro>
<#if includeServices??>
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
<#if rxjsVersion?default(6) lt 6>import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';<#else>import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';</#if>
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

    constructor(private http: HttpClient) {
        super();
    }

<#list module.methods?sort_by("name") as method>
    public ${method.name}(<@paramList params=method.params/>): Observable<${method.returnType}> {
        return this.http.request('${method.httpMethod}', ${module.className}.$$getServiceRoot() + `${method.methodPath}`, ${module.className}.defaultRequestOptions(<#if method.body??>${method.body}</#if>))
                <#if rxjsVersion?default(6) lt 6>.map(${module.className}.extractData)
                .catch(${module.className}.handleError);<#else>.pipe(map(${module.className}.extractData), catchError(${module.className}.handleError));</#if>
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