import {Response, RequestOptions, Headers, Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {Observer} from 'rxjs/Observer';
import 'rxjs/add/observable/throw';
import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';


class RuntimeContext {

    // change it
    private localTokenId: string = null;
    private globalTokenKey: string = null;
    public root: string = 'http://localhost:8080/jaxrs';
    public pollingTimeout: number = 10;

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

    protected defaultRequestOptions(httpMethod: string): RequestOptions {
        const tokenId = runtimeContext.getTokenId();
        const headers = new Headers({
            'content-type': 'application/json',
            // 自行添加
            'X-CLIENT-PROVIDER': 'CONCRETE-ANGULAR'
        });
        if (tokenId) {
            headers.append('CONCRETE-TOKEN-ID', tokenId);
        }
        return new RequestOptions({
            method: httpMethod,
            headers: headers,
            withCredentials: true
        });
    }

    protected extractData(res: Response) {
        if (res.headers) {
            runtimeContext.setTokenId(res.headers.get('concrete-token-id'));
        }
        if (res.status == 204) {
            return null;
        }

        const contentType = res.headers.get("content-type");
        if (contentType !== null && contentType.toLowerCase().startsWith('text/plain')) {
            return res.text();
        } else {
            return res.json();
        }
    }

    private static onError(code: number, message: String): ErrorInfo {
        const errorInfo: ErrorInfo = new ErrorInfo(code, message);
        // TODO: change it
        console.log(errorInfo.toString());
        return errorInfo;
    }

    protected handleError(error: Response | any) {
        const errorInfo = error.json() || {};
        return Observable.throw(AbstractConcreteService.onError(
            errorInfo.code || error.status,
            errorInfo.msg || error.statusText));
    }
}


@Injectable()
export class Broadcast extends AbstractConcreteService {

    private pollingStart: boolean = false;
    private bcSubject: Map<string, Subject<any>> = new Map();

    constructor(private http: Http) {
        super();
    }

    private notify(messages: any[], pollingFunc: Function) {
        if (messages && messages.length > 0) {
            for (const message of messages) {
                const subject = this.bcSubject.get(message.subject);
                if (subject !== null) {
                    subject.next(message.body);
                } else {
                    console.warn("no subscriber for " + message.subject);
                }
            }
        }
        setTimeout(pollingFunc, 10);
    }

    private polling(timeOut: number): Observable<any> {
        return this.http.request(this.$$getServiceRoot() + `/Concrete/polling/${timeOut}`, this.defaultRequestOptions('GET'))
            .map(this.extractData)
            .catch(this.handleError);
    }

    public doPolling(){
        if (!this.pollingStart) {
            const self = this;
            const pollingFunc = function () {
                self.polling(runtimeContext.pollingTimeout).subscribe(
                    messageArray => self.notify(messageArray, pollingFunc),
                    () => self.pollingStart = false);
                self.pollingStart = true;
            }
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