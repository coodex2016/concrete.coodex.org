export declare type Int = number;
export declare type Float = number;

declare interface EnumItem<T>{
    key: string;
    value: T;
}
export declare class BaseEnum<T> {
    /**
     * 作废，命名拼写错误，某个枚举值的标签
     * @deprecated
     */
    _lableOf: (v: T) => string;
    /**
     * 某个枚举值的标签
     * @deprecated
     */
    _labelOf: (v: T) => string;
    /**
     * 常量的键值列表
     * @deprecated
     */
    _toArray: (values:T[])=>EnumItem<T>[];
    /**
     * 某个枚举值的标签
     */
    labelOf: (v: T) => string;
    /**
     * 常量的键值列表
     */
    toArray: (values:T[])=>EnumItem<T>[];
}

