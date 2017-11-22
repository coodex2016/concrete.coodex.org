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
        "pollingTimeout": 10,
        "onError": function (code, msg) {
            alert("errorCode:" + code + "\nerrorMsg:" + msg);
        },
        "onBroadcast": function (msgId, host, subject, data) {
            console.log(data);
        }
    };


    var configuration = default_configuration;

    var encode = function (any) {
        if (any === undefined || any === null)
            return "";
        else
            return encodeURIComponent(any.toString());
    }

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

    var invoke = function (executable) {
        if (executable) {
            var pathNodes = executable.path.split("/");
            var url = "";
            var param = $.extend({}, executable.param);
            for (var i = 0; i < pathNodes.length; i++) {
                var node = pathNodes[i];
                if (!node && node.trim() === "") continue;
                if (node.charAt(0) === "{") {
                    var key = node.substr(1, node.length - 2);
                    node = param[key];
                    delete param[key];
                }
                url += "/" + encode(node);
            }

            var data = {};
            if (Object.keys(param).length > 0) {
                var obj = Object.keys(param).length === 1 ? param[Object.keys(param)[0]] : param;
                data = {
                    data: typeof(obj) === 'string' ? obj : JSON.stringify(obj)
                }
            }

            var headers = { 'X-CLIENT-PROVIDER': 'CONCRETE-jQuery' };
            var tokenId = getTokenId();
            if(tokenId)headers["CONCRETE_TOKEN_ID"] = tokenId;

            return $.ajax($.extend({}, data, {
                url: configuration.root + url,
                type: executable.method,
                contentType: "application/json; charset=utf-8",
                dataType: executable.dataType,
                headers: headers,
                crossDomain: true,
                xhrFields: {
                    withCredentials: true
                },
                success: function(data, textStatus, request){
                    setTokenId(request.getResponseHeader('CONCRETE_TOKEN_ID'));
                }
            })).error(function (jx) {
                if (configuration.onError) {
                    var e = jx.responseJSON;
                    if(typeof e === "object"){
                        configuration.onError(e.code,e.msg);
                    } else {
                        configuration.onError(jx.status, jx.responseText);
                    }
                                        // configuration.onError(this, arguments);
                }
            });
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
        "polling": function(){
            try{
                if(!this.pollingStart){
                    var pollingModule = this.module("org.coodex.concrete.jaxrs.Polling");
                    var self = this;
                    var pollingFunc = function(){
                        if(!self.pollingStart) return;
                        pollingModule.polling(configuration.pollingTimeout).done(function(messages){
                            if(configuration.onBroadcast && messages && messages.length > 0){
                                for(var i = 0; i < messages.length; i ++){
                                    var msg = messages[i];
                                    try{
                                        configuration.onBroadcast(msg.id, msg.host, msg.subject, msg.body);
                                    }catch(e){}
                                }
                            }
                            setTimeout(pollingFunc, 10);
                        }).error(function(){
                            this.pollingStart = false;
                        })
                    }
                    this.pollingStart = true;
                    pollingFunc();
                }
            }catch(e){}
        },
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

    register("Polling", "org.coodex.concrete.jaxrs", { "polling": function (timeOut) {return invoke({"path": "/Concrete/polling/{timeOut}","param": {"timeOut": timeOut},"method": "GET", "dataType": "json" });}});

    register("Calc", "org.coodex.practice.jaxrs.api", { "add": function (x, y) {return invoke({"path": "/Calc/add/{x}/{y}","param": {"x": x, "y": y},"method": "GET", "dataType": "json" });}, "subscribe": function () {return invoke({"path": "/Calc/subscribe","param": {},"method": "GET", "dataType": "json" });}});

    register("ServiceExample", "org.coodex.practice.jaxrs.api", { "add": function (x, y) {return invoke({"path": "/ServiceExample/ServiceB/A/Calc/add/{x}/{y}","param": {"x": x, "y": y},"method": "GET", "dataType": "json" });}, "all": function () {return invoke({"path": "/ServiceExample/ServiceB/all","param": {},"method": "GET", "dataType": "json" });}, "genericTest": function (x) {return invoke({"path": "/ServiceExample/genericTest","param": {"x": x},"method": "POST", "dataType": "json" });}, "genericTest1001": function (x) {return invoke({"path": "/ServiceExample/GenericTest/genericTest1001","param": {"x": x},"method": "POST", "dataType": "json" });}, "genericTest1002": function (x) {return invoke({"path": "/ServiceExample/GenericTest/genericTest1002","param": {"x": x},"method": "POST", "dataType": "json" });}, "tokenId": function () {return invoke({"path": "/ServiceExample/tokenId","param": {},"method": "GET", "dataType": "text" });}, "subscribe": function () {return invoke({"path": "/ServiceExample/ServiceB/A/Calc/subscribe","param": {},"method": "GET", "dataType": "json" });}, "update": function (bookId, book) {return invoke({"path": "/ServiceExample/ServiceB/A/{bookId}","param": {"bookId": bookId, "book": book},"method": "PUT", "dataType": "text" });}, "delete": function (bookId) {return invoke({"path": "/ServiceExample/ServiceB/A/{bookId}","param": {"bookId": bookId},"method": "DELETE", "dataType": "text" });}, "g5": function (xx) {return invoke({"path": "/ServiceExample/g5","param": {"xx": xx},"method": "POST", "dataType": "json" });}, "findByPriceLessThen": function (price) {return invoke({"path": "/ServiceExample/ServiceB/priceLessThen/{price}","param": {"price": price},"method": "GET", "dataType": "json" });}, "g6": function (gp) {return invoke({"path": "/ServiceExample/g6","param": {"gp": gp},"method": "POST", "dataType": "json" });}, "multiPojo": function (pathParam, body1, body2, body3, body4) {return invoke({"path": "/ServiceExample/multiPojo/{pathParam}","param": {"pathParam": pathParam, "body1": body1, "body2": body2, "body3": body3, "body4": body4},"method": "POST", "dataType": "json" });}, "bigStringTest": function (arg0, arg1) {return invoke({"path": "/ServiceExample/ServiceB/A/bigStringTest/{arg0}","param": {"arg0": arg0, "arg1": arg1},"method": "POST", "dataType": "text" });}, "get": overload("get", {"1": function (bookId) {return invoke({"path": "/ServiceExample/ServiceB/A/{bookId}","param": {"bookId": bookId},"method": "GET", "dataType": "json" });}, "2": function (author, price) {return invoke({"path": "/ServiceExample/ServiceB/A/{author}/{price}","param": {"author": author, "price": price},"method": "GET", "dataType": "json" });}}), "genericTest5": function (gp) {return invoke({"path": "/ServiceExample/genericTest5","param": {"gp": gp},"method": "POST", "dataType": "json" });}, "findByAuthorLike": function (author) {return invoke({"path": "/ServiceExample/ServiceB/authorLike/{author}","param": {"author": author},"method": "GET", "dataType": "json" });}, "genericTest2": function (y) {return invoke({"path": "/ServiceExample/genericTest2","param": {"y": y},"method": "POST", "dataType": "json" });}, "genericTest4": function (gp) {return invoke({"path": "/ServiceExample/genericTest4","param": {"gp": gp},"method": "POST", "dataType": "json" });}, "checkRole": function () {return invoke({"path": "/ServiceExample/ServiceB/A/checkRole","param": {},"method": "GET", "dataType": "text" });}, "genericTest3": function (z) {return invoke({"path": "/ServiceExample/genericTest3","param": {"z": z},"method": "POST", "dataType": "json" });}});

    register("SaaSExample", "org.coodex.practice.jaxrs.api", { "exampleForSaaS": function (tenantId, ok) {return invoke({"path": "/SaaS/{tenantId}/Test/exampleForSaaS/{ok}","param": {"tenantId": tenantId, "ok": ok},"method": "GET", "dataType": "text" });}});

    if(self){
        self.concrete = concrete;
    }
    return concrete;

}));