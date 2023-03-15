/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export declare function getTokenId(): string;
export declare function saveTokenId(tokenId: string): void;

export declare function getTokenId(moduleName: string): string;
export declare function saveTokenId(tokenId: string, moduleName: string): void;

declare type BroadcastCallback = (msgId:string, host:string, subject:string, data:any) =>void;
declare type ErrorCallback = (code: number, msg: string) =>void;
declare type WarningCallback = ErrorCallback;


export interface ConcreteOptions {
    /**
     * 请求根
     */
    root?: string;
    /**
     * error handle
     */
    onError?: ErrorCallback;
    /**
     * warning handle
     */
    onWarning?: WarningCallback;
    /**
     * polling time out
     */
    pollingTimeout?: number;
    /**
     * global token key. 多系统共用token时使用。不推荐
     */
    globalTokenKey?: string;
    /**
     * 是否开启请求数据混淆，需要后端开启混淆插件
     */
    grable?: Boolean;
    /**
     * 全局header
     */
    headers?: {[key:string]:string};
    /**
     * token存在哪，默认sessionStorage
     */
    storage?: Storage;
    /**
     * broadcast handle
     */
    onBroadcast?: BroadcastCallback;
}

interface Concrete {
    /**
     * 设置concrete的全局选项
     * @param options
     */
    configure(options: ConcreteOptions): void;
    configure(moduleName: string, options: ConcreteOptions): void;
}

export declare const concrete: Concrete;

export default concrete;
