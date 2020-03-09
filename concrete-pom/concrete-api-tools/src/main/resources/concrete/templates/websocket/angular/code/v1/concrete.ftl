<#macro classNameList classes><#list classes?sort as clazz>${clazz}<#if clazz_has_next>, </#if></#list></#macro>
import { NgModule } from '@angular/core';
<#list services?keys?sort as key>
import { <@classNameList classes=services[key]/> } from './${key}';
</#list>

export * from './AbstractConcreteService';
<#list packages?sort as pack>
export * from './${pack}';
</#list>


@NgModule({
    declarations: [],
    imports: [],
    providers: [<@classNameList classes=providers/>],
})
export class Concrete${moduleType}Module {
}