'use strict';

(function (factory) {
    if (typeof exports === "object" && typeof module === "object") // CommonJS
        module.exports = factory(require('jquery'));
    else if (typeof define === "function" && define.amd) // AMD
        define(['jquery'], factory);
    else { // Plain browser env
        var self = this || window;
        self.concrete = factory(self.$, self);
    }
}(function ($, self) {

    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c == 'x' ? r : (r & 0x7 | 0x8)).toString(16);
        });
        return uuid;
    };

    /**
     * 根据方法参数数量重载方法
     * @param funcName
     * @param function_map
     * @returns {Function}
     */
    var overload = function (funcName, function_map) {

        return function () {
            var key = arguments.length.toString();
            var func = function_map[key];
            if (!func && typeof func !== "function") {
                throw new Error(
                    "not found function: " + funcName + " with " + key + " parameter(s).");
            }
            return func.apply(this, arguments);
        }
    };

    var default_configuration = {
        "root": "",
        "onError": function (code, msg) {
            alert("errorCode:" + code + "\nerrorMsg:" + msg);
        },
        "onBroadcast": function (data) {
            console.log(data);
        }
    };

    var configuration = default_configuration;


    var localTokenId = null;

    var getTokenId = function(){
        return (configuration.globalTokenKey ?
        localStorage.getItem(configuration.globalTokenKey) : null) || localTokenId;
    }

    var setTokenId = function(tokenId){
        if(!tokenId) return;
        localTokenId = tokenId;
        if(configuration.globalTokenKey){
            localStorage.setItem(configuration.globalTokenKey, tokenId);
        }
    }

    var session;// web socket session

    var registry = function () {
        var map = {};
        return {
            get: function (msgId) {
                return msgId ? map[msgId] : null;
            },
            set: function (msgId, data) {
                map[msgId] = data;
            },
            pick: function (msgId) {
                var reg = this.get(msgId);
                if (reg) {
                    delete map[msgId];
                }
                return reg;
            }
        }
    }();

    var msgHandle = function (msg) {
        var data = JSON.parse(msg);

        if(data.subjoin && data.subjoin.broadcast){
            if(configuration.onBroadcast)
                configuration.onBroadcast(data.msgId, data.subjoin.hostId, data.subjoin.subject, data.content);
            return;
        }
        setTokenId(data.concreteTokenId);
        var reg = registry.pick(data.msgId);

        if(!reg){
            console.warn("invalid msg:" + msg );
            return;
        }

        if(data.ok){
            reg.def.resolve(data.content);
        } else {
            reg.def.reject(data.content);
        }
    }

    var send = function (executable) {
        var d = $.Deferred();
        var msgId = generateUUID();
        var reg = {
            def: d,
            future: setTimeout(function () {
                var reg = registry.pick(msgId);
                if (reg) {
                    reg.def.reject({
                        clientSide: true,
                        code: 99998,
                        msg: 'timeOut'
                    })
                }
            }, 15 * 60 * 1000)
        };
        registry.set(msgId, reg);

        var retryTimes = 0, maxRetryTimes = 5, delay = 50;
        var dataPackage = {
            msgId: msgId,
            serviceId: executable.serviceId,
            content: executable.param
        }
        var tokenId = getTokenId();
        if(tokenId)dataPackage.concreteTokenId = tokenId;

        var _send = function(){
            var str = JSON.stringify(dataPackage);
            if(session.readyState === 1){
                session.send(str);
            } else {
                if(retryTimes ++ > maxRetryTimes){
                    registry.pick(msgId);
                    clearTimeout(reg.future);
                    d.reject({
                        clientSide: true,
                        code: 99998,
                        msg: 'maximum retried.'
                    });
                } else {
                    setTimeout(_send, delay);
                }
            }
        }

        _send();
        return d.promise().fail(function(e){
            if (configuration.onError) {
                if (typeof e === "object") {
                    configuration.onError(e.code, e.msg);
                } else {
                    configuration.onError();
                }
            }
        });
    }

    var invoke = function (executable) {
        if (executable) {
            if(!session || session.readyState > 1){
                session = new WebSocket(configuration.root);
                session.onmessage = function (p1) {
                    msgHandle(p1.data);
                };
            }

            return send(executable);
        } else {
            throw new Error("executable object is null.");
        }
    };

    var modules = {};
    var clean = function (fullName) {
        var nodes = fullName.split(".");
        var result = undefined;
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i] && nodes[i].trim() !== "") {
                if (result) {
                    result = result + "." + nodes[i];
                } else {
                    result = nodes[i];
                }
            }
        }
        return result;
    };

    var parseModule = function (fullName) {
        var packageName = undefined;
        var nodes = clean(fullName).split(".");
        for (var i = 0; i < nodes.length - 1; i++) {
            if (packageName) {
                packageName += "." + nodes[i];
            } else {
                packageName = nodes[i];
            }
        }
        return {
            "package": packageName,
            "module": nodes[nodes.length - 1]
        }
    };

    var concrete = {
        "configure": function (config) {
            configuration = $.extend({}, default_configuration, config);
        },
        "module": function () {
            if (arguments.length == 0 || arguments.length > 2) {
                throw new Error("IllegalArgument, arguments must (moduleName) or (package.moduleName) or (moduleName, package).");
            }
            var fullName = arguments.length === 1 ? arguments[0] : (arguments[1] + "." + arguments[0]);
            var info = parseModule(fullName);
            var m = modules[info.module];
            var module = undefined;
            if (m) {
                if (info.package) {
                    module = m[info.package];
                } else {
                    if (Object.keys(m).length === 1) {
                        module = m[Object.keys(m)[0]];
                    }
                }
            }

            if (module)
                return module;

            throw new Error("No module found. " + fullName);
        }
    };


    /**
     * 注册一个模块，由代码生成器调用
     * @param moduleName 模块名，既Interface的ClassName
     * @param packageName 包名，既Interface的packageName
     * @param module 该模块的所有方法
     */
    var register = function (moduleName, packageName, module) {
        if (!modules[moduleName]) {
            modules[moduleName] = {};
        }
        modules[moduleName][packageName] = module;
    };

<#list modules as m>
    ${m}
</#list>
    if(self){
        self.${moduleName} = concrete;
    }
    return concrete;

}));