/* eslint-disable */
import * as axios from 'axios'

let defaultConfiguration = {
    'root': undefined,
    'onError': undefined,
    'pollingTimeout': 10,
    'globalTokenKey': undefined,
    'onBroadcast': undefined
}

let setPollingState = function (module, state) {
    module ? concrete.configure(module, {
        pollingState: state
    }) : concrete.configure({
        pollingState: state
    })
}

let _polling = function (module) {
    let onBroadcast = getConfigItem(module, 'onBroadcast')
    let pollingTimeout = getConfigItem(module, 'pollingTimeout')
    execute(module, `/Concrete/polling/${r'${pollingTimeout}'}`, 'json', 'get').then((value) => {
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
        setTimeout(function () {
            _polling(module)
        }, 1)
    }).catch(reason => {
        setPollingState(module, false)
    })
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
        let module = arguments.length === 0 ? 'concrete': arguments[0]
        let pollingState = getConfigItem(module, 'pollingState')
        if (!pollingState) {
            setPollingState(module, true)
            _polling(module)
        }
    }
}

concrete.configure({
    'root': '/jaxrs',
    'onError': function (code, msg) {
        console.error(['errorCode: ', code, '; errorMsg: ', msg].join(''))
    },
    'pollingTimeout': 10,
    'globalTokenKey': undefined,
    'onBroadcast': function (msgId, host, subject, data) {
        console.log(['msgId: ', msgId, '; host: ', host, '; subject: ', subject, '; data: ', data].join(''))
    }
})

export default concrete

function getConfigItem (module, key) {
    if (concrete.configuration[module] && concrete.configuration[module][key]) {
        return concrete.configuration[module][key]
    } else {
        return concrete.configuration[key]
    }
}

let tokens = {}

function getTokenId (module) {
    let globalTokenKey = getConfigItem(module, 'globalTokenKey')
    return (globalTokenKey ? localStorage.getItem(globalTokenKey) : null) ||
        (tokens[module] && tokens[module].localTokenId)
}

function setTokenId (module, tokenId) {
    if (tokenId) {
        if (!tokens[module]) {
            tokens[module] = {}
        }
        tokens[module].localTokenId = tokenId
        let globalTokenKey = getConfigItem(module, 'globalTokenKey')
        if (globalTokenKey) {
            localStorage.setItem(globalTokenKey, tokenId)
        }
    }
}

function setTokenIdFromResponseHeaders (module, headers) {
    let tokenId = headers['concrete-token-id']
    if (tokenId) {
        setTokenId(module, tokenId)
    }
}

function _onError (module, err) {
    let onError = getConfigItem(module, 'onError')
    if (onError && typeof onError === 'function') {
        onError(err.code, err.errorMsg)
    }
}

export function argumentsError (module) {
    let err = {code: 1, errorMsg: 'arguments error'}
    _onError(module, err)
    return new Promise((resolve, reject) => {
        reject(err)
    })
}

export function execute (module, url, responseType, method, body) {
    let headers = {
        'Cache-Control': 'no-cache, no-store',
        'content-type': 'application/json',
        'X-CLIENT-PROVIDER': 'CONCRETE-AXIOS ${version}'
    }
    let tokenId = getTokenId(module)
    if (tokenId) {
        headers['CONCRETE-TOKEN-ID'] = tokenId
    }
    let options = {
        withCredentials: true,
        url: getConfigItem(module, 'root') + url,
        method: method,
        responseType: responseType,
        maxRedirects: 0,
        headers: headers
    }
    if (body) options.data = body

    return axios.create().request(options).then((response) => {
        setTokenIdFromResponseHeaders(module, response.headers)
        return Promise.resolve(response.data)
    }).catch((error) => {
        console.log(error)
        let err = {code: 0, errorMsg: 'unknown'}
        if (error.response) {
            let headers = error.response.headers
            setTokenIdFromResponseHeaders(module, headers)
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
        _onError(module, err)
        return Promise.reject(err)
    })
}


export function overload (module, function_map) {

    return function () {
        var key = arguments.length.toString();
        var func = function_map[key];
        if (!func && typeof func !== "function") {
            return argumentsError(module);
        }
        return func.apply(this, arguments);
    }
};