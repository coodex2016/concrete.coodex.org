/* tslint:disable */
import {HttpClient, HttpResponse, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent} from '@angular/common/http';
import {Injectable} from '@angular/core';
<#if rxjsVersion?default(6) lt 6>import {Observable} from 'rxjs/Observable';
import {Observer} from 'rxjs/Observer';
import 'rxjs/add/observable/throw';<#else>import {Observable, Observer, Subject, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';</#if>

export class RuntimeContext {

    public localTokenId: string = null;
    public globalTokenKey: string = null;
    public root = 'http://localhost:8080/jaxrs';
    public pollingTimeout = 10;
    // 统一异常处理，返回值为true，说明已被处理
    public errorHandle = (errorInfo: ErrorInfo) => {
        console.log(errorInfo);
        return false;
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
            observe: 'response',
            withCredentials: true
        };
    }

    protected static extractData(result: any) {
        const res: HttpResponse<any> = result;
        if (res.headers) {
            runtimeContext.setTokenId(res.headers.get('concrete-token-id'));
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
        const errorInfo = typeof res.error === 'object' ? res.error : (JSON.parse(res.error) || {});
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
        let concreteHeaders = req.headers.set('content-type', 'application/json')
            .set('Cache-Control', 'no-cache, no-store')
            .set('X-CLIENT-PROVIDER', 'CONCRETE-ANGULAR-V2-${version}');
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
        return this.http.request(<#if style>'GET', Broadcast.$$getServiceRoot() + `/Concrete/polling/${timeOut}`, Broadcast.defaultRequestOptions()<#else>'POST', Broadcast.$$getServiceRoot() + `/Concrete/polling`, Broadcast.defaultRequestOptions(timeOut)</#if>)
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
