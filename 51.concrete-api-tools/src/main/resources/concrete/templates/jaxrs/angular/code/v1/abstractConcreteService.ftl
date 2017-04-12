// 定义抽象ConcreteService的行为, Concrete公用单元

import { Response, RequestOptions, ResponseContentType, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';

export abstract class AbstractConcreteService {

    protected $$getServiceRoot(): string {
        return ConcreteCommon.getServiceRoot(this.$$belong());
    }

    protected abstract $$belong(): string;

    protected defaultRequestOptions(httpMethod: string): RequestOptions {
        return new RequestOptions({
            method: httpMethod,
            headers: new Headers({
                'content-type': 'application/json',
                // 自行添加
                'X-CLIENT-PROVIDER': 'CONCRETE-ANGULAR'
            }),
            withCredentials: true,
            responseType: ResponseContentType.Json
        });
    }

    protected extractData(res: Response){
        const result = res.json() || {};
        // if(type of result === 'string')
        //    result =
        return result;
    }

    protected handleError (error: Response | any) {
        const errorInfo = error.json() || {};
        return Observable.throw(ConcreteCommon.onError(
                errorInfo.code || error.status,
                errorInfo.msg || error.statusText ));
    }
}

class ConcreteCommon {

    // TODO: change it
    static defaultServiceRoot = 'http://localhost:8080';

    static serviceRootMap = {
        // TODO: change it
        'serverName1': 'serviceRoot',
        'serverName2': 'serviceRoot'
    };

    public static getServiceRoot(name: string): string {
        if (name === null)
            return ConcreteCommon.defaultServiceRoot;
        else
            return ConcreteCommon.serviceRootMap[name];
    }

    public static onError(code: number, message: String): ErrorInfo {
        const errorInfo: ErrorInfo = new ErrorInfo(code, message);
        // TODO: change it
        console.log(errorInfo.toString());
        return errorInfo;
    }
}

export class ErrorInfo {
    private code: number;
    private errorMsg: string;

    constructor(code, errorMsg) {
        this.code = code;
        this.errorMsg = errorMsg;
    }

    public getCode(): number {
        return this.code;
    }

    public getErrorMsg(): string {
        return this.errorMsg;
    }

    public toString(): string {
        return 'errorCode: ' + this.getCode() + '; errorMsg: ' + this.getErrorMsg();
    }
}
