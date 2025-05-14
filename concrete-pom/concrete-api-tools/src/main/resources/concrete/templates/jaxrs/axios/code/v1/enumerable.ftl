import { valueOf, toArray } from './constUtil'
export default {
<#list elements as e>
    /**
     * label: ${e.label}
     * desc: ${e.desc}
     * value: ${e.value}
     */
    ${e.key}: ${e.codeValue},
</#list>
    _lableOf(v) {
        return this.labelOf(v)
    },
    _labelOf(v) {
        return this.labelOf(v)
    },
    labelOf(v){
        const o = valueOf(this, v)
        if (o) return o.key
        throw 'not found: ' + v
    },
    _toArray(values) {
        return this.toArray(values)
    },
    toArray(values){
        return toArray(this, values)
    }
}