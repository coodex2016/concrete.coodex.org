/* eslint-disable */
import * as axios from 'axios'

const axiosAdaptor = axios.VERSION && axios.VERSION.startsWith('1.') ? axios.default : axios

const CONCRETE_CLIENT_PROVIDER = 'CONCRETE-AXIOS-${version}'

let defaultConfiguration = {
    root: '/jaxrs',
    onError: function (code, msg) {
        console.error(['errorCode: ', code, '; errorMsg: ', msg].join(''))
    },
    onWarning: function (code, msg) {
        console.warn(['warning: code: ', code, '; message: ', msg].join(''))
    },
    pollingTimeout: 10,
    globalTokenKey: 'concrete-token-id',
    grable: false,
    storage: sessionStorage,
    onBroadcast: function (msgId, host, subject, data) {
        console.log(
            ['msgId: ', msgId, '; host: ', host, '; subject: ', subject, '; data: ', data].join('')
        )
    }
}

let setPollingState = function (moduleName, state) {
    moduleName
        ? concrete.configure(moduleName, { pollingState: state })
        : concrete.configure({ pollingState: state })
}

/**
 * CancellablePromise proxy
 * @param {Promise} promise 原promise对象
 * @param {function} cancelAction 取消的具体操作
 * @returns
 */
function cancellableProxy(promise, cancelAction, state) {
    return promise // 代理功能待测试
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
    if (!tokens[moduleName]) {
        tokens[moduleName] = {}
    }
    if (tokenId) {
        tokens[moduleName].localTokenId = tokenId
        let globalTokenKey = getConfigItem(moduleName, 'globalTokenKey')
        if (globalTokenKey) {
            getStorage(moduleName).setItem(globalTokenKey, tokenId)
        }
    } else {
        delete tokens[moduleName]['localTokenId']
        let globalTokenKey = getConfigItem(moduleName, 'globalTokenKey')
        if (globalTokenKey) {
            getStorage(moduleName).removeItem(globalTokenKey)
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

function _onWarning(moduleName, warning) {
    let onWarning = getConfigItem(moduleName, 'onWarning')
    if (onWarning && typeof onWarning === 'function') {
        for (let i = 0; i < warning.length; i++) {
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
    let customHeaders = getConfigItem(moduleName, 'headers') || {}
    let baseHeaders = {}
    for (let key in customHeaders) {
        let v = customHeaders[key]
        baseHeaders[key] = typeof v === 'function' ? v() : v
    }
    let headers = Object.assign({}, baseHeaders, {
        'Cache-Control': 'no-cache, no-store',
        'content-type': 'application/json',
        'X-CLIENT-PROVIDER': CONCRETE_CLIENT_PROVIDER
    })
    let tokenId = getTokenId(moduleName)
    if (tokenId) {
        headers['CONCRETE-TOKEN-ID'] = tokenId
    }
    for (let key in headers) {
        if (headers[key] === undefined || headers[key] === null) delete headers[key]
    }

    const controller = new AbortController()
    let options = {
        withCredentials: true,
        url: getConfigItem(moduleName, 'root') + url,
        method: method,
        responseType: responseType,
        maxRedirects: 0,
        signal: controller.signal,
        headers: headers
    }
    if (body) options.data = body

    return cancellableProxy(
        axiosAdaptor
            .create()
            .request(options)
            .then((response) => {
                setTokenIdFromResponseHeaders(moduleName, response.headers)
                if (response.headers['concrete-warnings']) {
                    _onWarning(
                        moduleName,
                        JSON.parse(decodeURIComponent(response.headers['concrete-warnings']))
                    )
                }
                return Promise.resolve(response.status === 204 ? null : response.data)
            })
            .catch((error) => {
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

// grable invoker

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
            'x-invoker-provider': GRABLE_INVOKER
        },
        content: JSON.stringify(payload),
        serviceId
    }
    let tokenId = getTokenId(moduleName)
    if (tokenId) {
        req.concreteTokenId = tokenId
    }

    const controller = new AbortController()
    let path = getConfigItem(moduleName, 'root')
    if (path && path.charAt(path.length - 1) != '/') {
        path += '/'
    }
    return cancellableProxy(
        axiosAdaptor
            .create({
                method: 'post',
                responseType: 'arraybuffer',
                headers: {
                    'Content-Type': 'application/x-concrete-bin'
                }
            })
            .post(path, new Blob([xor(RequestPackage.encode(RequestPackage.create(req)).finish())]))
            .then((res) => {
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
                            errorMsg: errorInfo.msg
                        }
                        _onError(moduleName, err)
                        throw err
                    }
                } else {
                    throw res
                }
            })
            .catch((error) => {
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

execute = overload('concrete', {
    5: executeJaxrs,
    4: executeJaxrs,
    3: grableExecute,
    2: grableExecute
})
// end grable invoker

export { execute, concrete }

let _polling = function (moduleName) {
    let onBroadcast = getConfigItem(moduleName, 'onBroadcast')
    let pollingTimeout = getConfigItem(moduleName, 'pollingTimeout')
    let pollingPromise = grable(moduleName)
        ? execute(moduleName, '3d1e308a7dc5b661625718ad7905e5150e55614c', {
              timeOut: pollingTimeout
          })
        : execute(moduleName, `/Concrete/polling`, 'json', 'POST', { timeOut: pollingTimeout })
    pollingPromise
        .then((value) => {
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
            this.configuration = Object.assign(
                {},
                this.configuration || defaultConfiguration,
                arguments[0]
            )
            return this
        } else if (!this.configuration[arguments[0]]) {
            this.configuration[arguments[0]] = Object.assign({}, defaultConfiguration)
        }
        this.configuration[arguments[0]] = Object.assign(
            this.configuration[arguments[0]] || {},
            arguments[1]
        )
        return this
    },
    polling: function () {
        let moduleName = arguments.length === 0 ? 'concrete' : arguments[0]
        let pollingState = getConfigItem(moduleName, 'pollingState')
        if (!pollingState) {
            setPollingState(moduleName, true)
            _polling(moduleName)
        }
    }
}

concrete.configure({})

export default concrete
