# 错误号信息

| 编号 | 信息模版 | key | FieldName |
| ---: | --- | --- | --- |
<#list errorInfo as e>
| ${e.errorCode?string('000000')} | ${(e.errorMessage!"None description.")?html?replace("|","&#124;")} | ${e.key!"-"} | ${e.fieldName!"-"} |
</#list>

