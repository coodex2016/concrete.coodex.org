/* eslint-disable */
import * as axios from 'axios'

const CONCRETE_CLIENT_PROVIDER = "CONCRETE-AXIOS-${version}"

let defaultConfiguration = {
    root: '/jaxrs',
    onError: function (code, msg) {
        console.error(['errorCode: ', code, '; errorMsg: ', msg].join(''))
    },
    onWarning: function(code, msg){
        console.warn(['warning: code: ', code, '; message: ', msg].join(''))
    },
    pollingTimeout: 10,
    globalTokenKey: 'concrete-token-id',
    grable: false,
    storage: sessionStorage,
    onBroadcast: function (msgId, host, subject, data) {
        console.log(['msgId: ', msgId, '; host: ', host, '; subject: ', subject, '; data: ', data].join(''))
    },
}

let setPollingState = function (moduleName, state) {
    moduleName ? concrete.configure(moduleName, { pollingState: state }) : concrete.configure({ pollingState: state })
}

let _polling = function (moduleName) {
    let onBroadcast = getConfigItem(moduleName, 'onBroadcast')
    let pollingTimeout = getConfigItem(moduleName, 'pollingTimeout')
    executeJaxrs(moduleName, `/Concrete/polling`, 'json', 'post', pollingTimeout)
        .then(value => {
            if (typeof onBroadcast === 'function' && value.length > 0) {
                for (let i = 0; i < value.length; i++) {
                    try {
                        let msg = value[i]
                        onBroadcast(msg.id, msg.host, msg.subject, msg.body)
                    } catch (e) {
                        console.error(e)
                    }
                }
            }
            setTimeout(() => _polling(moduleName), 1)
        })
        .catch(() => setPollingState(moduleName, false))
}

let concrete = {
    configure: function () {
        if (arguments.length === 1) {
            this.configuration = Object.assign({}, this.configuration || defaultConfiguration, arguments[0])
            return this
        } else if (!this.configuration[arguments[0]]) {
            this.configuration[arguments[0]] = Object.assign({}, defaultConfiguration)
        }
        this.configuration[arguments[0]] = Object.assign(this.configuration[arguments[0]] || {}, arguments[1])
        return this
    },
    polling: function () {
        let moduleName = arguments.length === 0 ? 'concrete' : arguments[0]
        let pollingState = getConfigItem(moduleName, 'pollingState')
        if (!pollingState) {
            setPollingState(moduleName, true)
            _polling(moduleName)
        }
    },
}

concrete.configure({})

export default concrete

/**
 * CancellablePromise proxy
 * @param {Promise} promise 原promise对象
 * @param {function} cancelAction 取消的具体操作
 * @returns
 */
function cancellableProxy(promise, cancelAction, state) {
    if (promise instanceof Promise) {
        let _stat = state || { cancelled: false }
        let _resolve, _reject

        const isCannelled = () => {
            return _stat && _stat.cancelled
        }

        const next = promise.then(
            d => {
                if (isCannelled()) return
                let r = _resolve && _resolve(d)
                if (r !== undefined) return r
            },
            e => {
                if (isCannelled()) return
                if (_reject) {
                    let r = _reject(e)
                    if (r !== undefined) throw r
                } else {
                    throw e
                }
            }
        )

        // 代理then方法
        promise.then = (resolve, reject) => {
            _resolve = resolve
            _reject = reject
            return cancellableProxy(next, cancelAction, _stat)
        }

        // 代理catch方法
        promise.catch = reject => {
            _reject = reject
            return cancellableProxy(next, cancelAction, _stat)
        }

        // 增加取消方法
        promise.cancel = () => {
            typeof cancelAction === 'function' && cancelAction()
            _stat.cancelled = new Date()
        }

        return promise
    }
}

function getConfigItem(moduleName, key) {
    if (!moduleName) moduleName = 'concrete'
    if (concrete.configuration[moduleName] && concrete.configuration[moduleName][key]) {
        return concrete.configuration[moduleName][key]
    } else {
        return concrete.configuration[key]
    }
}

let tokens = {}

function getStorage(moduleName) {
    return getConfigItem(moduleName, 'storage') || sessionStorage
}

export function getTokenId(moduleName) {
    let globalTokenKey = getConfigItem(moduleName, 'globalTokenKey')
    return (
        (globalTokenKey ? getStorage(moduleName).getItem(globalTokenKey) : null) ||
        (tokens[moduleName] && tokens[moduleName].localTokenId)
    )
}

export function saveTokenId(tokenId, moduleName) {
    return setTokenId(moduleName, tokenId)
}

function setTokenId(moduleName, tokenId) {
    if (tokenId) {
        if (!tokens[moduleName]) {
            tokens[moduleName] = {}
        }
        tokens[moduleName].localTokenId = tokenId
        let globalTokenKey = getConfigItem(moduleName, 'globalTokenKey')
        if (globalTokenKey) {
            getStorage(moduleName).setItem(globalTokenKey, tokenId)
        }
    }
}

function setTokenIdFromResponseHeaders(moduleName, headers) {
    let tokenId = headers['concrete-token-id']
    if (tokenId) {
        setTokenId(moduleName, tokenId)
    }
}

function _onError(moduleName, err) {
    let onError = getConfigItem(moduleName, 'onError')
    if (onError && typeof onError === 'function') {
        onError(err.code, err.errorMsg)
    }
}

function _onWarning(moduleName, warning){
    let onWarning = getConfigItem(moduleName, 'onWarning')
    if(onWarning && typeof onWarning === 'function'){
        for(let i = 0; i < warning.length; i ++){
            onWarning(warning[i].code, warning[i].message)
        }
    }
}

export function argumentsError(moduleName) {
    let err = { code: 1, errorMsg: 'arguments error' }
    _onError(moduleName, err)
    return Promise.reject(err)
}

function executeJaxrs(moduleName, url, responseType, method, body) {
    let headers = {
        'Cache-Control': 'no-cache, no-store',
        'content-type': 'application/json',
        'X-CLIENT-PROVIDER': CONCRETE_CLIENT_PROVIDER,
    }
    let tokenId = getTokenId(moduleName)
    if (tokenId) {
        headers['CONCRETE-TOKEN-ID'] = tokenId
    }

    const controller = new AbortController()
    let options = {
        withCredentials: true,
        url: getConfigItem(moduleName, 'root') + url,
        method: method,
        responseType: responseType,
        maxRedirects: 0,
        signal: controller.signal,
        headers: headers,
    }
    if (body) options.data = body

    return cancellableProxy(
        axios
            .create()
            .request(options)
            .then(response => {
                setTokenIdFromResponseHeaders(moduleName, response.headers)
                if(response.headers['concrete-warnings']){
                    _onWarning(moduleName, JSON.parse(decodeURIComponent(response.headers['concrete-warnings'])))
                }
                return Promise.resolve(response.status === 204 ? null : response.data)
            })
            .catch(error => {
                let err = { code: 0, errorMsg: 'unknown' }
                if (error.response) {
                    let headers = error.response.headers
                    setTokenIdFromResponseHeaders(moduleName, headers)
                    if (headers['concrete-error-occurred']) {
                        err.code = error.response.data.code
                        err.errorMsg = error.response.data.msg
                    } else {
                        err.code = error.response.status
                        err.errorMsg = error.response.statusText
                    }
                } else if (error.request) {
                    err.errorMsg = error.message
                }
                _onError(moduleName, err)
                return Promise.reject(err)
            }),
        () => controller.abort()
    )
}

let execute = executeJaxrs

export function overload(moduleName, function_map) {
    return function () {
        let key = arguments.length.toString()
        let func = function_map[key]
        if (!func && typeof func !== 'function') {
            return argumentsError(moduleName)
        }
        return func.apply(this, arguments)
    }
}

<#if grableEnabled?default(true)>// grable invoker

export function grable(moduleName) {
    return getConfigItem(moduleName, 'grable')
}

import protobufjs from 'protobufjs'

const GRABLE_INVOKER = 'concrete-axios-grable-${version}'
const Type = protobufjs.Type,
    Field = protobufjs.Field,
    MapField = protobufjs.MapField

function xor(buf) {
    return buf.map((v, i) => v ^ (0xff << i % 8))
}

const RequestPackage = new Type('RequestPackage')
    .add(new Field('compressed', 1, 'bytes'))
    .add(new Field('content', 2, 'string'))
    .add(new MapField('subjoin', 3, 'string', 'string'))
    .add(new Field('concreteTokenId', 4, 'string'))
    .add(new Field('serviceId', 5, 'string'))

const ResponsePackage = new Type('ResponsePackage')
    .add(new Field('compressed', 1, 'bytes'))
    .add(new Field('content', 2, 'string'))
    .add(new MapField('subjoin', 3, 'string', 'string'))
    .add(new Field('concreteTokenId', 4, 'string'))
    .add(new Field('ok', 5, 'bool'))

export function grableExecute(moduleName, serviceId, payload) {
    let req = {
        subjoin: {
            'x-invoker-provider': GRABLE_INVOKER,
        },
        content: JSON.stringify(payload),
        serviceId,
    }
    let tokenId = getTokenId(moduleName)
    if (tokenId) {
        req.concreteTokenId = tokenId
    }

    const controller = new AbortController()
    return cancellableProxy(
        axios
            .create({
                method: 'post',
                responseType: 'arraybuffer',
                headers: {
                    'Content-Type': 'application/x-concrete-bin',
                },
            })
            .post(
                getConfigItem(moduleName, 'root'),
                new Blob([xor(RequestPackage.encode(RequestPackage.create(req)).finish())])
            )
            .then(res => {
                if (res.status === 200) {
                    let d = ResponsePackage.decode(xor(new Uint8Array(res.data))).toJSON()
                    let tokenId = d.concreteTokenId
                    if (tokenId) setTokenId(moduleName, tokenId)
                    if (d.subjoin && d.subjoin['CONCRETE-WARNINGS']) {
                        _onWarning(moduleName, JSON.parse(d.subjoin['CONCRETE-WARNINGS']))
                    }
                    if (d.ok) {
                        return JSON.parse(d.content)
                    } else {
                        let errorInfo = JSON.parse(d.content)
                        let err = {
                            code: errorInfo.code,
                            errorMsg: errorInfo.msg,
                        }
                        _onError(moduleName, err)
                        throw err
                    }
                } else {
                    throw res
                }
            })
            .catch(error => {
                let err = { code: 0, errorMsg: 'unknown' }
                if (error.response) {
                    err.code = error.response.status
                    err.errorMsg = error.response.statusText
                } else if (error.request) {
                    err.errorMsg = error.message
                } else {
                    throw error
                }
                _onError(moduleName, err)
                throw err
            }),
        () => controller.abort()
    )
}

execute = overload('concrete', { 5: executeJaxrs, 4: executeJaxrs, 3: grableExecute, 2: grableExecute })
// end grable invoker

</#if>export { execute }
