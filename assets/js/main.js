function addEventListener(el, eventName, func, useCapture) {
    if (window.attachEvent) {
        el.attachEvent('on' + eventName, func)
    } else if (window.addEventListener) {
        if (useCapture != undefined && useCapture === true) {
            el.addEventListener(eventName, func, true)
        } else {
            el.addEventListener(eventName, func, false)
        }
    } else {

    }
}

// 判断浏览器是否支持 Storage 存储对象
// sessionStorage（会话存储）
// localStorage（本地存储）
function isSupportWebStorage() {
    if (typeof (Storage) !== "undefined") {
        return true;
    } else {
        return false;
    }
}

// 使用浏览器存储对象
function getWebStorage(storageName) {
    var storage;
    if (storageName == "sessionStorage") {
        storage = window.sessionStorage;
    } else if (storage == "localStorage") {
        storageName = window.localStorage;
    } else {
        storage = window.sessionStorage;
    }

    // setItem
    var setItem = function (key, value, expires) {
        var params = {
            key,
            value,
            expires
        };
        if (expires && expires > 0) {
            // 时间单位 毫秒
            params = Object.assign(params, { startTime: new Date().getTime() });
        } else {
            params.expires = 0;
        }
        storage.setItem(key, JSON.stringify(params));
    };

    // getItem
    var getItemByKey = function (key) {
        var str = storage.getItem(key);
        if (str) {
            var params;
            try {
                params = JSON.parse(str);
            } catch (error) {
                params = null;
            }
            if (params && params.expires) {
                if (params.expires <= 0) {
                    return params.value;
                } else {
                    var nowTime = new Date().getTime();
                    if (nowTime - params.startTime >= params.expires) {
                        // removeItem
                        storage.removeItem(key);
                        return null;
                    } else {
                        return params.value;
                    }
                }
            } else {
                return str;
            }
        } else {
            return null;
        }
    };

    // removeItem
    var removeItemByKey = function (key) {
        storage.removeItem(key);
    };

    // 将 localStorage 中的某个键值对删除
    var removeItemByKeyPrefix = function (key_prefix) {
        for (var i = 0; i < storage.length; i++) {
            var key = storage.key(i);
            if (key.startsWith(key_prefix)) {
                storage.removeItem(key);
            }
        }
    };

    // 将 localStorage 的所有内容清除
    var removeAll = function () {
        storage.clear();
    };

    return {
        setItem,
        getItemByKey,
        removeItemByKey,
        removeItemByKeyPrefix,
        removeAll
    };
}

// 使用服务器器存储对象
function getServerStorage() {
    var getForObject = function (url, success, error) {
        ajax({
            method: "get",
            dataType: "json",
            timeout: 20000,
            url: url
        }, success, error)
    };

    var postForObject = function (url, data, success, error) {
        ajax({
            data: data,
            method: "post",
            dataType: "json",
            timeout: 20000,
            url: url
        }, success, error)
    };
    return { getForObject, postForObject };
}

// ajax
function ajax(options, success, error) {
    var method = (options.method || 'GET').toUpperCase();
    var url = options.url;
    var async = options.async === false ? false : true;
    // 预期服务器返回的数据类型
    var dataType = (options.dataType || 'text').toLowerCase();
    // 请求超时时间（毫秒）
    var timeout = options.timeout || 5000;
    var body = options.data || null;
    var headers = {};

    // XMLHttpRequest
    var xmlHttp;
    if (window.XMLHttpRequest) {
        xmlHttp = new XMLHttpRequest();
    } else {
        xmlHttp = new ActiveXObject('Microsoft.XMLHTTP');
    }

    if (async) {
        // 默认值为 0，意味着没有超时。
        xmlHttp.timeout = timeout;
    }

    xmlHttp.onreadystatechange = function () {
        // 0: 请求未初始化
        if (xmlHttp.readyState == 0) {
            console.log("0");
        }
        // 1: 服务器连接已建立
        if (xmlHttp.readyState == 1) {
            console.log("0-->1");
        }
        // 2: 请求已接收
        if (xmlHttp.readyState == 2) {
            console.log("1-->2");
        }
        // 3: 请求处理中
        if (xmlHttp.readyState == 3) {
            console.log("2-->3");
        }
        // 4: 请求已完成，且响应已就绪
        if (xmlHttp.readyState == 4) {
            var httpStatus = xmlHttp.status;
            var errorThrown;
            // 判断响应结果
            if (xmlHttp.status == 200) {
                // 成功 OK
                var result;
                if (dataType == "xml") {
                    result = xmlHttp.responseXML;
                } else if (dataType == "html") {
                    result = xmlHttp.responseText;
                } else if (dataType == "script") {
                    result = xmlHttp.responseText;
                } else if (dataType == "json") {
                    try {
                        result = JSON.parse(xmlHttp.responseText);
                    } catch (error) {
                        result = xmlHttp.responseText;
                    }
                } else if (dataType == "jsonp") {
                    result = xmlHttp.responseText;
                } else if (dataType == "text") {
                    result = xmlHttp.responseText;
                } else {
                    var responseHeaders = xmlhttp.getAllResponseHeaders();
                    var lastModified = xmlHttp.getResponseHeader('Last-Modified');
                    var contentLength = xmlHttp.getResponseHeader('Content-Length');
                    var contentType = xmlHttp.getResponseHeader('Content-type');
                    if (contentType.includes("xml")) {
                        result = xmlHttp.responseXML;
                    } else if (contentType.includes("json")) {
                        result = JSON.parse(xmlHttp.responseText);
                    } else {
                        result = xmlHttp.responseText;
                    }
                }
                return success(result, httpStatus);
            } else {
                // 失败，需要根据响应码判断失败原因
                return error(httpStatus, errorThrown);
            }
        }
    };
    xmlHttp.ontimeout = function (event) {

    };
    xmlHttp.onabort = (event) => {

    };
    xmlHttp.onerror = (event) => {
        xmlHttp.abort();
    };
    // xmlHttp.addEventListener('error', handleEvent);
    // xmlHttp.addEventListener('abort', handleEvent);
    // xmlHttp.addEventListener("abort", (event) => {});
    // xmlHttp.addEventListener("error", (event) => {});

    // 建立与服务器的连接
    xmlHttp.open(method, url, async);


    // xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlHttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    for (const key in headers) {
        if (Object.hasOwnProperty.call(headers, key)) {
            const value = headers[key];
            xmlHttp.setRequestHeader(key, value);
        }
    }

    // 发送请求 GET请求不需要参数，POST请求需要把body部分以字符串 或者FormData对象传进去。
    xmlHttp.send(body);
}







// 将footer置于页面最底部
// $(function(){
//     function footerPosition(){
//         $("footer").removeClass("fixed-bottom");
//         var contentHeight = document.body.scrollHeight,
//             winHeight = window.innerHeight;
//         if(!(contentHeight > winHeight)){
//             $("footer").addClass("fixed-bottom");
//         } else {
//             $("footer").removeClass("fixed-bottom");
//         }
//     }
//     footerPosition();
//     $(window).resize(footerPosition);
// });
