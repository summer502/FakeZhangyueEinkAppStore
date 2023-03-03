/**
 * 伪掌阅iReader应用商店服务端，端口80
 * 有2种访问方式：
 * 1、拦截域名"ebook.zhangyue.com"，使用http get访问此80端口，此时只能访问"ebook.zhangyue.com:80"下的接口地址。反向代理模式。
 * 2、配置HTTP代理服务器，指定此80端口。正向代理模式。
 */

const fs = require('fs');
const os = require("os");
const http = require('http');
const url = require('url');
const querystring = require('querystring');
const util = require('util');
const path = require('path');

const fakeAppStoreService = require('./service/fakeAppStoreService.js');
const { httpProxyService, httpsProxyService } = require('./service/httpAgentService.js');
const journal = require('./service/journal.js')
const sse = require('./service/sse.js');

const port = 80;

// HTTP服务器，监听端口80
const httpServer = http.createServer(function (request, response) {
    //
    logRequest(request);

    const method = request.method;
    if (method === 'GET') {
        const requestUrl = url.parse(request.url, true);
        const query = requestUrl.query;
        const pathname = decodeURI(requestUrl.pathname);
        if (pathname === '/zybook3/app/app.php') {
            // 判断请求是否是应用商店的请求
            if (query.ca === 'Eink_AppStore.AppList' || query.ca === 'Eink_AppStore.AppInfo' || query.ca === 'Eink_AppStore.Category') {
                // 如果是应用商店的请求，就转给伪应用商店进行处理
                // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0
                // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink
                // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category
                fakeAppStoreService(request, response);
            } else {
                // 如果不是应用商店的请求，就原封不动，代理访问目标地址
                // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_Vip.Index
                // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_Shelf.BookUpdate&bookIds=12619203,11792737,11532597,11671501,11222331&tingBookIds=&albumIds=
                // http://ebook.zhangyue.com/zybook/u/p/api.php?Act=getSource&type=1
                httpProxyService(request, response);
            }
        } else if (pathname === '/' || pathname === '/index.html') {
            // http://127.0.0.1/index.html
            loadHtml(response, 'index.html');
        } else if (pathname === '/uploadapp' || pathname === '/uploadapp.html') {
            // http://127.0.0.1/uploadapp.html
            loadHtml(response, 'uploadapp.html');
        } else if (pathname === '/favicon.ico' || pathname.startsWith("/images/") || pathname.startsWith("/EinkAppStore")) {
            if (pathname === '/favicon.ico' || pathname.startsWith("/images/")) {
                // 下载文件
                downloadFile(request, response, "/html" + pathname);
            } else if (pathname === '/EinkAppStore') {
                // 重定向到“http://127.0.0.1:80/EinkAppStore/”
                console.log("重定向到 http://" + request.headers.host + "/EinkAppStore/");
                response.writeHead(301, {
                    Location: '/EinkAppStore/',
                    'Set-Cookie': 'fakeAppStore-token=caonima456; path=/EinkAppStore; httpOnly; expires=' + getCookieExpireTime() + ';'
                });
                response.end();
            } else {
                // 下载文件
                downloadFile(request, response, pathname);
            }
        } else if (pathname === '/server-status/sse') {
            let cookie = getCookie(request);
            console.log('sse----cookie====', cookie);
            // SSE
            sse.subscribe(request, response);

            // 自定义事件类型 server-status
            pushServerStatus();

            // 定时任务
            const pushServerStatusTimer = setInterval(function () {
                // 自定义事件类型 server-status
                pushServerStatus();
            }, 1000);

            // 绑定事件
            response.on('close', function () {
                clearInterval(pushServerStatusTimer);
            });
        } else if (pathname === '/server-status/ajax') {
            let cookie = getCookie(request);
            console.log('ajax----cookie====', cookie);
            // ajax轮询
            let lastlogid;
            let logarr;
            if (query.logid) {
                lastlogid = query.logid;
                if (lastlogid == 0) {
                    let len = theCache.httpServer["server-log"].logList.length;
                    if (len != 0) {
                        lastlogid = theCache.httpServer["server-log"].logList[len - 1].id;
                    }
                }
                // 
                logarr = theCache.httpServer["server-log"].logList.filter(function (item, index, arr) {
                    return lastlogid < item.id;
                });
                if (logarr.length != 0) {
                    lastlogid = logarr[logarr.length - 1].id;
                }
            } else {
                lastlogid = 0;
                logarr = [];
            }
            httpServer.getConnections(function (err, count) {
                console.log("connections: ", count, err);
            });
            let result = {
                "code": 0,
                "msg": "",
                "body": {
                    date: new Date(),
                    connections: httpServer._connections,
                    requests: theCache.httpServer["server-status"].requests,
                    platform: os.platform(),
                    totalMemory: os.totalmem(),
                    freeMemory: os.freemem()
                },
                "log": {
                    "logs": logarr,
                    "lastlogid": lastlogid
                }
            };
            theCache.httpServer["server-status"].pushid++;
            theCache.httpServer["server-log"].pushid++;

            response.setHeader('Set-Cookie', `lastlogid=${lastlogid};path=/server-status/ajax;httpOnly;expires=${getCookieExpireTime()};`);
            response.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
            response.end(JSON.stringify(result));
        } else {
            // 其他的请求，原封不动，代理访问目标地址
            httpProxyService(request, response);
        }
    } else if (method === 'POST') {
        const requestUrl = url.parse(request.url, true);
        const pathname = decodeURI(requestUrl.pathname);
        if (pathname === '/EinkAppStore/upload/app') {
            //文件上传
            doPost(request, response, function (request, response, bodyData) {
                let id = bodyData.id.data;
                let name = bodyData.name.data;
                let icon = bodyData.icon.data;
                if (bodyData.iconFile) {
                    let name = bodyData.iconFile.name;
                    let filename = bodyData.iconFile.filename;
                    let data = bodyData.iconFile.data;
                    let filepath = __dirname + '/../EinkAppStore/downloads/icon/' + filename;
                    fs.writeFile(filepath, data, err => {
                        if (err) {
                            console.log(err);
                        } else {
                            console.log('上传成功');
                        }
                    });
                }
                let appVersion = bodyData.appVersion.data;
                let appSize = bodyData.appSize.data;
                let categoryId = bodyData.categoryId.data;
                let appName = bodyData.appName.data;
                let appUrl = bodyData.appUrl.data;
                if (bodyData.appUrlFile) {
                    let name = bodyData.appUrlFile.name;
                    let filename = bodyData.appUrlFile.filename;
                    let data = bodyData.appUrlFile.data;
                    let filepath = __dirname + '/../EinkAppStore/downloads/zip/' + filename;
                    fs.writeFile(filepath, data, err => {
                        if (err) {
                            console.log(err);
                        } else {
                            console.log('上传成功');
                        }
                    });
                }
                let appDesc = bodyData.appDesc.data;
                let explain = bodyData.explain.data;

                response.writeHead(200, { 'Content-Type': 'text/html; charset=utf8' });
                // response.write('<script>location.href="/"</script>');
                // response.writeHead(301, { 'Location': '/' });
                response.end(util.inspect(bodyData));
            });
        } else if (pathname === '/EinkAppStore/delete') {
            let form = mFormDoPost();
            form.parse(request);
            form.onfield = (name, value) => {
                // 表单普通类型数据
            }
            form.onfile = (name, filename, file) => {
                // 表单文件类型数据
            }
            form.onclose = () => {
                // 表单解析完成
                response.writeHead(301, { 'Location': '/' });
                response.end();
            }
        } else {
            response.writeHead(404, { 'Content-Type': 'text/html; charset=utf8' });
            response.write("<h1>未找到</h1>");
            response.end();
        }
    } else {
        response.writeHead(200, { "content-type": 'text/html' });
        response.end("<h1>目前只支持：HTTP GET、HTTP POST。</h1>");
    }
}).on('connection', function (socket) {
    //
    console.log('新连接', socket.remoteAddress, socket.remotePort);
}).on('connect', function (request, socket, head) {
    // HTTP CONNECT，建立tcp连接
    httpsProxyService(request, socket);
}).on('request', function (request, response) {
    //
    console.log('新请求', request.method, request.url);
}).listen(80, function () {
    console.log('伪应用商店启动成功，访问地址 http://127.0.0.1:80/');
    console.log("");
});

const theCache = {
    httpServer: {
        serverIp: getServerIpAddress(),
        "server-status": {
            pushid: 0,
            requests: 0
        },
        "server-log": {
            pushid: 0,
            logList: new Array()
        }
    }
};
const responseSet = new Set();

// 读取html目录下的".html"文件
function loadHtml(response, fileName) {
    fs.readFile(__dirname + "/../html/" + fileName, 'utf8', function (err, data) {
        if (err) {
            console.error(err);
            response.writeHead(404, { 'Content-Type': 'text/html' });
            response.write('<h1>404 Not Found</h1>');
        } else {
            // response.setHeader('Content-type', 'text/css');
            // response.setHeader('Content-type', 'application/x-javascript');
            response.writeHead(200, {
                'Content-Type': 'text/html; charset=utf8',
                'Set-Cookie': 'fakeAppStore-token=caonima123; path=/; httpOnly; expires=' + getCookieExpireTime() + ';'
            });
            response.write(data.toString());
        }
        response.end();
    });
}

// 读取文件
function readFile(request, response, filePath) {

    let stream = fs.createReadStream(filePath);
    stream.on('data', function (chunk) {
        // response.write(chunk);
    }).on('end', function () {
        console.log("读取'" + filePath + "'文件完成。");
        // response.end();
    }).on('error', function (err) {
        console.error("读取'" + filePath + "'文件失败。" + err.message);
        response.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
        response.write("文件不存在");
        response.write("\n");
        response.write("----err：" + err);
        response.write("\n");
        response.write("----method：" + request.method);
        response.write("\n");
        response.write("----url：" + decodeURI(request.url));
        response.end();
    });

    stream.pipe(response);
}

// 获取"thePath"这个目录下的所有文件
function findAllFilesSync(thePath) {
    let result = [];
    function finderer(pathName) {
        let files = fs.readdirSync(pathName);
        files.forEach((item, index) => {
            let filePath = path.join(pathName, item);
            let stats = fs.statSync(filePath);
            if (stats.isDirectory()) {
                finderer(filePath);
            } else if (stats.isFile()) {
                result.push(filePath);
            } else {
                return console.error(stats);
            }
        });
    }
    finderer(thePath);
    return result;
}

// 如果是下载文件，就下载文件；如果是目录，就列出这个目录下的文件
function downloadFile(request, response, fileName) {
    let filepath = path.join(__dirname, '../', fileName);

    fs.stat(filepath, function (err, stats) {
        if (err) {
            console.error("读取" + fileName + "文件失败。" + err);
            response.writeHead(404, { 'Content-Type': 'text/html' });
            response.end(err.message);
        } else {
            console.log("读取" + fileName + "文件信息成功！");
            if (stats.isFile()) {
                // 文件类型是文件
                let fileStream = fs.createReadStream(filepath)
                    .pipe(response);
            } else if (stats.isDirectory()) {
                // 文件类型是目录
                fs.readdir(filepath, function (err, files) {
                    if (err) {
                        return console.error(err);
                    } else {
                        // 
                        let fileInfoList = [];
                        files.forEach(function (item, index) {
                            let stats = fs.statSync(filepath + "/" + item);

                            let fileInfo = {
                                "name": item,
                                "pathname": filepath + "/" + item,
                                "isDirectory": stats.isDirectory(),
                                "isFile": stats.isFile(),
                                "ctime": stats.ctime,
                                "mtime": stats.mtime,
                                "size": stats.size
                            };
                            fileInfoList.push(fileInfo);
                        })

                        // 页面
                        let fileIndexHtml = getFileIndexHtml(fileInfoList);

                        // 响应
                        response.writeHead(200, { 'Content-Type': 'text/html; charset=utf8' });
                        response.end(fileIndexHtml);
                    }
                });
            } else {
                console.error("未知的文件类型fileName:" + fileName + "。", stats);
                return;
            }
        }
    });
}

// 查找names数组中最长的字符串
function max(names) {
    let maxName = names[0];
    for (let i = 1; i < names.length; i++) {
        if (byteLength(names[i]) > byteLength(maxName)) {
            maxName = names[i];
        }
    }
    return maxName;
}

// 字符串的长度
function byteLength(str) {
    let b = 0;
    for (let i = 0; i < str.length; i++) {
        if (str.charCodeAt(i) <= 255) {
            b++;
        } else {
            //字符编码大于255，说明是双字节字符
            b += 2;
        }
    }
    return b;
}

// 生成"Index of /view-file/"页面
function getFileIndexHtml(fileInfoList) {
    // 填充空格
    function fillBlank(itemLen, maxLen) {
        let fillBlank = '';
        if (itemLen < maxLen) {
            for (let i = 0; i < maxLen - itemLen; i++) {
                fillBlank += ' ';
            }
        }
        return fillBlank;
    }
    // html
    let str = '<!DOCTYPE html>';
    str += '<html>';
    str += '<head><title>Index of /view-file/</title></head>';
    str += '<body>';
    str += '<h1>Index of /view-file/</h1>';
    str += '<hr>';
    str += '<pre>\r\n';
    str += '<a href="../">../</a>\r\n';
    // 文件名的长度的最大值
    let fileNames = fileInfoList.map(function (item, index, arr) {
        return item.name;
    });
    let maxfileNameLen = byteLength(max(fileNames));
    // 目录
    let allDir = fileInfoList.filter(function (item, index, arr) {
        return item.isDirectory;
    }).sort().forEach(function (item, index, arr) {
        let len = byteLength(item.name);
        let aurl = item.name + '/';
        str += '<a href="' + aurl + '">' + item.name + '/</a>      ' + fillBlank(len, maxfileNameLen) + item.mtime + '       ' + '-\r\n';
    });
    // 文件
    let allFile = fileInfoList.filter(function (item, index, arr) {
        return item.isFile;
    }).sort().forEach(function (item, index, arr) {
        let len = byteLength(item.name);
        let aurl = item.name;
        str += '<a href="' + aurl + '">' + item.name + '</a>       ' + fillBlank(len, maxfileNameLen) + item.mtime + '       ' + item.size + '\r\n';
    });;
    str += '</pre>';
    str += '<hr>';
    str += '</body>';
    str += '</html>';
    return str;
}

// 请求日志
function logRequest(request) {
    theCache.httpServer["server-status"].requests++;
    let ip = getClientIp(request);
    let id = getLogId(request);
    let time = Date.now();
    let logStr = ip + " --- [" + new Date().toISOString() + "] \"" + request.method + " " + request.url + "\" \"" + request.headers['user-agent'] + "\"";
    let logItem = {
        id,
        ip,
        time,
        logStr
    };

    theCache.httpServer["server-log"].logList.push(logItem);
    console.log(`access[${logItem.id}] ${ip} --- ${request.method} ${request.url}`);
    journal.writeAccessLog(logItem.logStr);
    pushServerLog(logItem);
}

function logError(err) {
    let logStr = new Date().toISOString() + " [" + err.code + "] " + err.name + ": " + err.message;
    journal.writeErrorLog(logStr);
}

// 自增id
function getLogId(request) {
    let id = Date.now();
    return id;
}

// 获取客户端的ip
function getClientIp(request) {
    let ip;
    let forwardedIpStr = request.headers['X-Forwarded-For'];
    if (forwardedIpStr) {
        let forwardedIpArray = forwardedIpStr.split(',');
        ip = forwardedIpArray[0];
    }
    if (!ip) {
        ip = request.socket.remoteAddress;
    }
    return ip;
}

//获取本机ip
function getServerIpAddress() {
    let interfaces = os.networkInterfaces();
    for (let devName in interfaces) {
        let iface = interfaces[devName];
        for (let i = 0; i < iface.length; i++) {
            let { family, address, mac, internal } = iface[i];
            if (family === 'IPv4' && address !== '127.0.0.1' && !internal) {
                return address;
            }
        }
    }
}

// 自定义事件类型 server-status
function pushServerStatus() {
    let result = {
        "code": 0,
        "msg": "",
        "body": {
            date: new Date(),
            connections: httpServer._connections,
            requests: theCache.httpServer["server-status"].requests,
            platform: os.platform(),
            totalMemory: os.totalmem(),
            freeMemory: os.freemem()
        }
    };
    let comment = null;
    let retry = 10000;
    let eventType = 'server-status';
    let eventId = theCache.httpServer["server-status"].pushid++;
    let eventData = JSON.stringify(result);

    sse.publish(comment, retry, eventType, eventId, eventData);
}

// 自定义事件类型 server-log
function pushServerLog(log) {
    let result = {
        "code": 0,
        "msg": "",
        "body": log
    };
    let comment = "this is log";
    let retry = 500;
    let eventType = 'server-log';
    let eventId = theCache.httpServer["server-log"].pushid++;
    let eventData = JSON.stringify(result);

    sse.publish(comment, retry, eventType, eventId, eventData);
}

//
function getCookieExpireTime() {
    let d = new Date();
    d.setTime(d.getTime() + (24 * 60 * 60 * 1000));
    return d.toUTCString();
}

//
function getCookie(request) {
    let cookie = {};
    let cookieStr = request.headers.cookie;
    let cookies = cookieStr ? cookieStr.split(';') : [];
    cookies.forEach((item) => {
        if (item) {
            let arr = item.split('=');
            if (arr && arr.length > 0) {
                let key = arr[0].trim();
                let value = arr[1] ? arr[1].trim() : undefined;

                cookie[key] = value;
            }
        }
    })
    return cookie;
}

//
function bufferSplit(buffer, splitter) {
    let arr = [];
    let splitterLength = Buffer.from(splitter).length;
    let start = 0
    let n = 0
    while ((n = buffer.indexOf(splitter, start)) !== -1) {
        arr.push(buffer.slice(start, n));
        start = n + splitterLength;
    }
    arr.push(buffer.slice(start));
    return arr;
}

//
function doPost(request, response, callback) {
    if (request.headers['content-type']) {
        let requestContentTypeArray = request.headers['content-type'].split(';');
        let enctype = requestContentTypeArray[0].trim();
        if (enctype == 'application/x-www-form-urlencoded') {
            // 表单数据编码类型enctype="application/x-www-form-urlencoded"
            let body = "";
            request.on('data', function (chunk) {
                body += chunk;
            });
            request.on('end', function () {
                // 解析参数
                let bodyData = querystring.parse(body);
                //
                callback.apply(this, [request, response, bodyData]);
            });
        } else if (enctype == 'text/plain') {
            // 表单数据编码类型enctype="text/plain"
            let body = "";
            request.on('data', function (chunk) {
                body += chunk;
            });
            console.log('body=', body);
        } else if (enctype == 'multipart/form-data') {
            // 表单数据编码类型enctype="multipart/form-data"
            let boundary = requestContentTypeArray[1].trim().split('=')[1];
            let chunks = [];
            let length = 0;
            request.on('data', function (chunk) {
                chunks.push(chunk);
                length += chunk.length;
            });
            request.on("end", function () {
                let bodyBuffer = Buffer.concat(chunks, length);
                // 解析参数
                let results = bufferSplit(bodyBuffer, '--' + boundary);
                results.pop();// 去掉最后一个"--"
                results.shift();// 去掉第一个""
                let bodyData = {};
                results.forEach(buffer => {
                    let delimiter1 = "\r\n";
                    let delimiter2 = "\r\n\r\n";
                    let n = buffer.indexOf(delimiter2);
                    let info = buffer.slice(delimiter1.length, n);
                    let data = buffer.slice((n + delimiter2.length), (buffer.length - delimiter1.length));
                    // console.log(info.toString());
                    // console.log(data.toString());
                    if (info.indexOf('\r\n') != -1) {
                        let arr = info.toString().split('\r\n');
                        let line1arr = arr[0].split(': ');
                        let line2arr = arr[1].split(': ');
                        // line1arr
                        let Content_Disposition = line1arr[1].split('; ');
                        let name = Content_Disposition[1].split('=')[1];
                        name = name.substring(1, name.length - 1);
                        console.log('filename----------', info.toString().match(/filename=".*"/g)[0].split('"')[1]);
                        let filename = Content_Disposition[2].split('=')[1];
                        filename = filename.substring(1, filename.length - 1);
                        // line2arr
                        let Content_Type = line2arr[1];

                        bodyData[name] = {
                            name,
                            filename,
                            data
                        };
                    } else {
                        let line1arr = info.toString().split(': ');
                        let Content_Disposition = line1arr[1].split('; ');
                        let name = Content_Disposition[1].split('=')[1];
                        name = name.substring(1, name.length - 1);

                        bodyData[name] = {
                            name,
                            data: data.toString()
                        };
                    }
                })
                //
                callback.call(this, request, response, bodyData);
            });
        } else {
            //
            response.writeHead(500, { 'Content-Type': 'text/html; charset=utf8' });
            response.write("<h1>500 Server Error</h1>");
            response.write("<br>");
            response.write("<h1>未知的表单'enctype'</h1>");
            response.end();
        }
    } else {
        //
        response.writeHead(500, { 'Content-Type': 'text/html; charset=utf8' });
        response.write("<h1>500 Server Error</h1>");
        response.write("<br>");
        response.write("<h1>找不到请求头'content-type'</h1>");
        response.end();
    }
}

// post form
function mFormDoPost() {
    let uploadDir;
    let dof = {};
    dof.parse = function (request) {
        if (request.headers['content-type']) {
            let requestContentTypeArray = request.headers['content-type'].split(';');
            let enctype = requestContentTypeArray[0].trim();
            if (enctype == 'application/x-www-form-urlencoded') {
                // 表单数据编码类型enctype="application/x-www-form-urlencoded"
                let body = "";
                request.on('data', function (chunk) {
                    body += chunk;
                });
                request.on('end', function () {
                    Promise.resolve().then(() => {
                        // 解析参数
                        let bodyData = querystring.parse(body);
                        for (const key in bodyData) {
                            // apply
                            dof.onfield.apply(this, [key, bodyData[key]]);
                        }
                        //
                        dof.onclose();
                    }).catch((err) => {
                        console.log(err);
                    });
                });
            } else if (enctype == 'text/plain') {
                // 表单数据编码类型enctype="text/plain"
                let body = "";
                request.on('data', function (chunk) {
                    body += chunk;
                });
                request.on('end', function () {
                    // 解析参数
                    let bodyData = querystring.parse(body);
                    console.log('body=', body);
                });
            } else if (enctype == 'multipart/form-data') {
                // 表单数据编码类型enctype="multipart/form-data"
                let boundary = requestContentTypeArray[1].trim().split('=')[1];
                let chunks = [];
                let length = 0;
                request.on('data', function (chunk) {
                    chunks.push(chunk);
                    length += chunk.length;
                });
                request.on("end", function () {
                    let bodyBuffer = Buffer.concat(chunks, length);
                    let p = new Promise((resolve, reject) => {
                        // 解析参数
                        let results = bufferSplit(bodyBuffer, '--' + boundary);
                        resolve(results);
                    });
                    p.then((results) => {
                        results.pop();// 去掉最后一个"--"
                        results.shift();// 去掉第一个""
                        results.forEach(buffer => {
                            let delimiter1 = "\r\n";
                            let delimiter2 = "\r\n\r\n";
                            let n = buffer.indexOf(delimiter2);
                            let info = buffer.slice(delimiter1.length, n);
                            let data = buffer.slice((n + delimiter2.length), (buffer.length - delimiter1.length));
                            // console.log(info.toString());
                            // console.log(data.toString());
                            if (info.indexOf('\r\n') != -1) {
                                let arr = info.toString().split('\r\n');
                                let line1arr = arr[0].split(': ');
                                let line2arr = arr[1].split(': ');
                                // line1arr
                                let Content_Disposition = line1arr[1].split('; ');
                                let name = Content_Disposition[1].split('=')[1];
                                name = name.substring(1, name.length - 1);
                                console.log('filename----------', info.toString().match(/filename=".*"/g)[0].split('"')[1]);
                                let filename = Content_Disposition[2].split('=')[1];
                                filename = filename.substring(1, filename.length - 1);
                                // line2arr
                                let Content_Type = line2arr[1];
                                // call
                                dof.onfile.call(this, name, filename, data);
                            } else {
                                let line1arr = info.toString().split(': ');
                                let Content_Disposition = line1arr[1].split('; ');
                                let name = Content_Disposition[1].split('=')[1];
                                name = name.substring(1, name.length - 1);
                                // call
                                dof.onfield.call(this, name, data.toString());
                            }
                        })
                        //
                        dof.onclose.call(this);
                    }, (err) => {
                        console.log(err);
                    });
                });
            } else {
                console.log('未知的表单数据编码类型');
            }
        } else {
            console.log("找不到请求头'content-type'");
        }
    };

    dof.onfield = function (name, value) {
    };

    dof.onfile = function (name, filename, file) {
    };

    dof.onclose = function () {
    };

    dof.onerror = function (err) {
    };

    return dof;
}

