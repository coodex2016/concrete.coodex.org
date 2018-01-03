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
        "onBroadcast": function (msgId, host, subject, data) {
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
        "polling": function(){},
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

    register("SaaSExample", "org.coodex.practice.jaxrs.api", { "exampleForSaaS": function (tenantId, ok) {return invoke({"serviceId": "82FB17B4A7E8343D55A57E7E6E764601063D3240","param": {"tenantId": tenantId, "ok": ok} });}});
    register("ServiceExample", "org.coodex.practice.jaxrs.api", { "genericTest": function (x) {return invoke({"serviceId": "29E4C7D6CBC5F8B04992D742D6CD8C75338EBFEA","param": x });}, "all": function () {return invoke({"serviceId": "3D22D8E971A86505E454071BAE873C995D8BA344","param": undefined });}, "add": function (x, y) {return invoke({"serviceId": "0298C57EA3CC54E99993E2973605A483F9295168","param": {"x": x, "y": y} });}, "genericTest1001": function (x) {return invoke({"serviceId": "6B03298353A066981570D6A193B380E204E9CE6B","param": x });}, "genericTest1002": function (x) {return invoke({"serviceId": "E8ED4C6B11618CB81AA07FDB64070E56AC1B0FAB","param": x });}, "tokenId": function () {return invoke({"serviceId": "382E719B18E470546037B313504535DD2BE58111","param": undefined });}, "subscribe": function () {return invoke({"serviceId": "148C75531BEF418A96AC3D92879EEF5F0E6D7C0E","param": undefined });}, "update": function (bookId, book) {return invoke({"serviceId": "2C2D2CB3456FC97272D7767108C166B49E2AC3F4","param": {"bookId": bookId, "book": book} });}, "delete": function (bookId) {return invoke({"serviceId": "CE3B9E1E4AC73DADD1FE0FBAFF11D3209B6D7784","param": bookId });}, "g5": function (xx) {return invoke({"serviceId": "E80BBFB137019CAC6232458A737DED5B5C2A5929","param": xx });}, "g6": function (gp) {return invoke({"serviceId": "F24CF2C09F088C92CFF8ED01E9F2B9227A88DDA6","param": gp });}, "findByPriceLessThen": function (price) {return invoke({"serviceId": "2CF247ABA7D55EF6523AA10DD3974F93A6CF7DE0","param": price });}, "multiPojo": function (pathParam, body1, body2, body3, body4) {return invoke({"serviceId": "26A79FCE1822D0067EE9215785541EA7C70CFA91","param": {"pathParam": pathParam, "body1": body1, "body2": body2, "body3": body3, "body4": body4} });}, "bigStringTest": function (pathParam, toPost) {return invoke({"serviceId": "390920F46693DB8276D8CA5E893758852B5F5804","param": {"pathParam": pathParam, "toPost": toPost} });}, "get": overload("get", {"1": function (bookId) {return invoke({"serviceId": "A5921572538BDCAA773D788F3A9292F3CB18B34F","param": bookId });}, "2": function (author, price) {return invoke({"serviceId": "8FD81C76EB63054900FEE71E4CCD730DE739DFA8","param": {"author": author, "price": price} });}}), "genericTest5": function (gp) {return invoke({"serviceId": "39D40DB4BC0D379A7D350C2562E0C3BC1E5B9D75","param": gp });}, "genericTest2": function (y) {return invoke({"serviceId": "529F4FE6F3ABA5343680B69C69D45BBADAE47ED6","param": y });}, "findByAuthorLike": function (author) {return invoke({"serviceId": "3DF9E532CE7440BA15897EC6ECFC15A99A8E7A11","param": author });}, "genericTest4": function (gp) {return invoke({"serviceId": "48E10D008E269501607D0FFFB00108A2E427EB98","param": gp });}, "genericTest3": function (z) {return invoke({"serviceId": "B8C1DE8D5430363980C92ED20B54B246D8002E61","param": z });}, "checkRole": function () {return invoke({"serviceId": "CB0503884AE338DCBEF69A149C12864D7C7F89C5","param": undefined });}});
    register("Calc", "org.coodex.practice.jaxrs.api", { "add": function (x, y) {return invoke({"serviceId": "29D006A09884E03E8E9549CA3957EE5C89A0FE8D","param": {"x": x, "y": y} });}, "subscribe": function () {return invoke({"serviceId": "0D3C6DE2973E971D0D9E14EF5716ECFE4996E8F2","param": undefined });}});
    if(self){
        self.concrete = concrete;
    }
    return concrete;

}));