/* tslint:disable */
import {HttpClient, HttpResponse, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
<#if rxjsVersion?default(6) lt 6>import {Observable} from 'rxjs/Observable';
import {Observer} from 'rxjs/Observer';
import 'rxjs/add/observable/throw';<#else>import {Observable, Observer, Subject, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';</#if>

export class RuntimeContext {

    public localTokenId: string = null;
    public globalTokenKey: string = null;
    public root = '/jaxrs';
    public pollingTimeout = 10;
    // 统一异常处理，返回值为true，说明已被处理
    public errorHandle = (errorInfo: ErrorInfo) => {
        console.log(errorInfo);
        return false;
    }
    // warning 统一处理
    public warningHandle = (warningInfo) => {
        console.log(warningInfo);
    }
	
    public getTokenId(): string {
        return (this.globalTokenKey ?
            localStorage.getItem(this.globalTokenKey) : null) || this.localTokenId;
    }

    public setTokenId(tokenId) {
        if (tokenId) {
            this.localTokenId = tokenId;
            if (this.globalTokenKey) {
                localStorage.setItem(this.globalTokenKey, tokenId);
            }
        }
    }
}

export const runtimeContext: RuntimeContext = new RuntimeContext();

export abstract class AbstractConcreteService {


    private static onError(code: number, message: string): ErrorInfo {
        const errorInfo: ErrorInfo = new ErrorInfo(code, message);
        let handled = false;
        if (runtimeContext.errorHandle && typeof runtimeContext.errorHandle === 'function') {
            handled = runtimeContext.errorHandle(errorInfo);
        }
        if (!handled) {return errorInfo; }
    }

    protected static $$getServiceRoot(): string {
        return runtimeContext.root;
    }

    protected static defaultRequestOptions(body?: any): any {
        return {
            responseType: 'text',
            body,
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Cache-Control': 'no-cache, no-store',
                'X-CLIENT-PROVIDER': 'CONCRETE-ANGULAR-V2-${version}'
            }),
            observe: 'response',
            withCredentials: true
        };
    }

    protected static extractData(result: any) {
        const res: HttpResponse<any> = result;
        if (res.headers) {
            runtimeContext.setTokenId(res.headers.get('concrete-token-id'));
            let warnings = res.headers.get('concrete-warnings');
            if(warnings && runtimeContext.warningHandle){
                let warningsArray = JSON.parse(decodeURIComponent(warnings));
                if (warningsArray instanceof Array){
                    for(let i =0;i < warningsArray.length; i ++){
                        try{
                            runtimeContext.warningHandle(warningsArray[i]);
                        }catch(e){
                            console.warn(e);
                        }
                    }
                }
            }
        }
        if (res.status === 204) {
            return null;
        }

        if (typeof res.body === 'object') {
            return res.body;
        }

        const contentType = res.headers.get('content-type');
        if (contentType !== null && contentType.toLowerCase().startsWith('text/plain')) {
            return res.body;
        } else {
            return JSON.parse(res.body);
        }
    }

    protected static handleError(res: Response | any) {
        const errorInfo = res.headers.get("concrete-error-occurred") ? JSON.parse(res.error) : {
            code: res.status,
            msg: res.error
        };
        const error = AbstractConcreteService.onError(errorInfo.code || res.status,
            errorInfo.msg || res.statusText);

        if (error) {
            return <#if rxjsVersion?default(6) lt 6>Observable.throw<#else>throwError</#if>(error);
        } else {
            return new Observable();
        }
    }
}

@Injectable()
export class ConcreteHeadersInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let concreteHeaders = req.headers;
        const tokenId = runtimeContext.getTokenId();
        if (tokenId) {
            concreteHeaders = concreteHeaders.set('CONCRETE-TOKEN-ID', tokenId);
        }
        return next.handle(req.clone({headers: concreteHeaders}));
    }
}

@Injectable()
export class Broadcast extends AbstractConcreteService {

    private pollingStart = false;
    private bcSubject: Map<string, Subject<any>> = new Map();

    constructor(private http: HttpClient) {
        super();
    }

    private notify(messages: any[], pollingFunc) {
        if (messages && messages.length > 0) {
            for (const message of messages) {
                const subject = this.bcSubject.get(message.subject);
                if (subject) {
                    subject.next(message.body);
                } else {
                    console.warn('no subscriber for ' + message.subject);
                }
            }
        }
        setTimeout(pollingFunc, 10);
    }

    private polling(timeOut: number): Observable<any> {
        return this.http.request('POST', Broadcast.$$getServiceRoot() + `/Concrete/polling`, Broadcast.defaultRequestOptions(timeOut))
            <#if rxjsVersion?default(6) lt 6>.map(Broadcast.extractData)
            .catch(Broadcast.handleError);<#else>.pipe(map(Broadcast.extractData), catchError(Broadcast.handleError));</#if>
    }

    public doPolling() {
        if (!this.pollingStart) {
            const self = this;
            const pollingFunc = () => {
                self.polling(runtimeContext.pollingTimeout).subscribe(
                    messageArray => self.notify(messageArray, pollingFunc),
                    () => self.pollingStart = false);
                self.pollingStart = true;
            };
            pollingFunc();
        }
    }

    public subscribe(subject: string, observer: Observer<any>): void {
        if (!this.bcSubject.get(subject)) {
            this.bcSubject.set(subject, new Subject());
        }
        this.bcSubject.get(subject).subscribe(observer);

        this.doPolling();
    }
}

export class ErrorInfo {
    code: number;
    errorMsg: string;

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
