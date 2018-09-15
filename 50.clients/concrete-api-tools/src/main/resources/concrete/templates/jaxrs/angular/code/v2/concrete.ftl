<#macro classNameList classes><#list classes?sort as clazz>${clazz}<#if clazz_has_next>, </#if></#list></#macro>
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ConcreteHeadersInterceptor, Broadcast } from './AbstractConcreteService';
<#list services?keys?sort as key>
import { <@classNameList classes=services[key]/> } from './${key}';
</#list>

export * from './AbstractConcreteService';
<#list packages?sort as pack>
export * from './${pack}';
</#list>


@NgModule({
    declarations: [],
    imports: [HttpClientModule],
    providers: [{
        provide: HTTP_INTERCEPTORS,
        useClass: ConcreteHeadersInterceptor,
        multi: true,
    }, Broadcast, <@classNameList classes=providers/>],
})
export class Concrete${moduleType}Module {
}
