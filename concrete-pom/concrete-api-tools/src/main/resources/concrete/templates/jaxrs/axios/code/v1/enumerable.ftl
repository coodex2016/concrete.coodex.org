export default {
<#list elements as e>
    /**
     * label: ${e.label}
     * desc: ${e.desc}
     * value: ${e.value}
     */
    ${e.key}: ${e.codeValue},
</#list>
}