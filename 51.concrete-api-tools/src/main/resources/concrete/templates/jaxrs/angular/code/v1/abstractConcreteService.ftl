// 定义抽象ConcreteService的行为, Concrete公用单元

import { Response, RequestOptions, ResponseContentType, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/throw';


class RuntimeContext{

    // change it
    private localTokenId: string = null;
    private globalTokenKey: string = null;
    public root: string = 'http://localhost:8080';

    public getTokenId (): string{
        return (this.globalTokenKey ?
            localStorage.getItem(this.globalTokenKey) : null) || this.localTokenId;
    }

    public setTokenId (tokenId){
        if (tokenId){
            this.localTokenId = tokenId;
            if (this.globalTokenKey){
                localStorage.setItem(this.globalTokenKey, tokenId);
            }
        }
    }
}

const runtimeContext: RuntimeContext = new RuntimeContext();

export abstract class AbstractConcreteService {

    protected $$getServiceRoot(): string {
        return runtimeContext.root;
    }

    protected defaultRequestOptions(httpMethod: string): RequestOptions {
        const tokenId = runtimeContext.getTokenId();
        const headers= new Headers({
            'content-type': 'application/json',
            // 自行添加
            'X-CLIENT-PROVIDER': 'CONCRETE-ANGULAR'
            });
        if (tokenId){
            headers.append('CONCRETE_TOKEN_ID', tokenId);
        }
        return new RequestOptions({
            method: httpMethod,
            headers: headers,
            withCredentials: true
        });
    }

    protected extractData(res: Response){
        if(res.headers){
            runtimeContext.setTokenId(res.headers.get('concrete_token_id'));
        }
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
