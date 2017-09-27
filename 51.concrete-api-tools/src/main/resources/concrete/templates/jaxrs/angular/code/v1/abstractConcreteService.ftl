// 定义抽象ConcreteService的行为, Concrete公用单元

import { Response, RequestOptions, ResponseContentType, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/throw';

export abstract class AbstractConcreteService {

    protected $$getServiceRoot(): string {
        // TODO change it
        return 'http://localhost:8080';
    }

    protected defaultRequestOptions(httpMethod: string): RequestOptions {
        return new RequestOptions({
            method: httpMethod,
            headers: new Headers({
                'content-type': 'application/json',
                // 自行添加
                'X-CLIENT-PROVIDER': 'CONCRETE-ANGULAR'
            }),
            withCredentials: true
        });
    }

    protected extractData(res: Response){
        return res.headers.get('content-type').toLowerCase().startsWith('text/plain') ? res.text() : (res.json() || null);
    }

    private static onError(code: number, message: String): ErrorInfo {
        const errorInfo: ErrorInfo = new ErrorInfo(code, message);
        // TODO: change it
        console.log(errorInfo.toString());
        return errorInfo;
    }

    protected handleError (error: Response | any) {
        const errorInfo = error.json() || {};
        return Observable.throw(AbstractConcreteService.onError(
                errorInfo.code || error.status,
                errorInfo.msg || error.statusText ));
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
