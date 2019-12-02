<#macro paramList params><#list params as param>${param.type} ${param.name}<#if param_has_next>, </#if></#list></#macro>
package ${package};

import org.coodex.concrete.api.rx.ReactiveExtensionFor;

import io.reactivex.Observable;

<#list imports?sort as import>
import ${import};
</#list>

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(${concreteClassName}.class)
public interface ${rxClassName} {

<#list methods as method>
    Observable<${method.returnType}> ${method.name}(<@paramList params=method.params/>);

</#list>

}