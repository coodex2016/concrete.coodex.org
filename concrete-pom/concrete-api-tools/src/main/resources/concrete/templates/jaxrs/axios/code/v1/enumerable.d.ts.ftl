import {Int, Float, BaseEnum} from './EnumBase'

declare class ${enumTypeName} extends BaseEnum<${valueType}>{
<#list elements as e>
    /**
    * label: ${e.label}
    * desc: ${e.desc}
    * value: ${e.value}
    */
    ${e.key}: ${valueType};
</#list>
}
declare const instance: ${enumTypeName};
export default instance;
