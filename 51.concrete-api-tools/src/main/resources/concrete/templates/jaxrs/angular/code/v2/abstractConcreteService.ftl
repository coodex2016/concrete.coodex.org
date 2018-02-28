import {HttpClient, HttpResponse, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Observer} from 'rxjs/Observer';
import 'rxjs/add/observable/throw';
import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';


class RuntimeContext {

    // change it
    private localTokenId: string = null;
    private globalTokenKey: string = null;
    public root = 'http://localhost:8080/jaxrs';
    public pollingTimeout = 10;

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

const runtimeContext: RuntimeContext = new RuntimeContext();

export abstract class AbstractConcreteService {

    protected $$getServiceRoot(): string {
        return runtimeContext.root;
    }

    protected defaultRequestOptions(body?: any): any {
        return {
            responseType: 'text',
            body: body,
            observe: 'response',
            withCredentials: true
        };
    }

    protected extractData(result: any) {
        const res: HttpResponse<any> = result;
        if (res.headers) {
            runtimeContext.setTokenId(res.headers.get('concrete-token-id'));
        }
        if (res.status === 204) {
            return null;
        }

        if(typeof res.body === 'object'){
            return res.body;
        }

        const contentType = res.headers.get('content-type');
        if (contentType !== null && contentType.toLowerCase().startsWith('text/plain')) {
            return res.body;
        } else {
            return JSON.parse(res.body);
        }
    }

    private static onError(code: number, message: String): ErrorInfo {
        const errorInfo: ErrorInfo = new ErrorInfo(code, message);
        // TODO: change it
        console.log(errorInfo.toString());
        return errorInfo;
    }

    protected handleError(res: Response | any) {
        const errorInfo = typeof res.error === 'object' ? res.error : (JSON.parse(res.error) || {});
        return Observable.throw(AbstractConcreteService.onError(
            errorInfo.code || res.status,
            errorInfo.msg || res.statusText));
    }
}

@Injectable()
export class ConcreteHeadersInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let concreteHeaders = req.headers.set('content-type', 'application/json')
            .set('Cache-Control','no-cache, no-store')
            .set('X-CLIENT-PROVIDER', 'CONCRETE-ANGULAR');
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

    private notify(messages: any[], pollingFunc: Function) {
        if (messages && messages.length > 0) {
            for (const message of messages) {
                const subject = this.bcSubject.get(message.subject);
                if (subject !== null) {
                    subject.next(message.body);
                } else {
                    console.warn('no subscriber for ' + message.subject);
                }
            }
        }
        setTimeout(pollingFunc, 10);
    }

    private polling(timeOut: number): Observable<any> {
        return this.http.request('GET', this.$$getServiceRoot() + `/Concrete/polling/${timeOut}`, this.defaultRequestOptions())
            .map(this.extractData)
            .catch(this.handleError);
    }

    public doPolling() {
        if (!this.pollingStart) {
            const self = this;
            const pollingFunc = function () {
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
