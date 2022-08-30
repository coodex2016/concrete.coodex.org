export default {
<#list elements as e>
    /**
     * label: ${e.label}
     * value: ${e.value}
     */
    ${e.key}: ${e.codeValue},
</#list>
}