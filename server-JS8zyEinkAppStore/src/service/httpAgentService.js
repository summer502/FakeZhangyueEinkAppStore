const http = require('http');
const url = require('url');
const net = require("net")
var dns = require('dns');

let dnsName = 'ebook.zhangyue.com';
let fakeIP;
dns.lookup(dnsName, function (err, address, family) {
    if (err) {
        console.log(err.stack);
        return;
    }
    console.log('解析"' + dnsName + '"的ip为: ' + address);
    fakeIP = address;
});

// http代理
function httpProxyService(request, response) {
    // GET http://www.hjkalhfka.jkhfk.com:8900/ljlioiikkk/path?sdfkah=jklasdhfk HTTP/1.1
    // Host: www.hjkalhfka.jkhfk.com:8900
    // User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0
    // Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8
    // Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
    // Accept-Encoding: gzip, deflate
    // Connection: keep-alive
    // Upgrade-Insecure-Requests: 1

    const requestUrl = url.parse(request.url);
    const { connection, host, ...originalHeaders } = request.headers;
    const options = {
        method: request.method,
        httpVersion: request.httpVersion,
        path: request.url || requestUrl.path,
        host: host,
        hostname: requestUrl.hostname || host.split(':')[0],
        port: requestUrl.port || host.split(':')[1] || 80,
        headers: originalHeaders
    };
    console.log(`------httpProxyService-----${JSON.stringify(options)}`);
    if (options.hostname == '127.0.0.1' || options.hostname == 'localhost' || request.socket.remoteAddress.indexOf(fakeIP) != -1) {
        console.log("httpProxyService--禁止代理访问自己");
        response.writeHead(500, { "content-type": 'text/html; charset=utf8' });
        response.end("<h1>禁止代理访问自己，防止请求递归循环访问</h1>");
        return;
    }
    const proxyRequest = http.request(options, function (proxyResponse) {
        response.writeHead(proxyResponse.statusCode, proxyResponse.headers);
        proxyResponse.pipe(response);

        // proxyResponse.setEncoding('utf8');
        proxyResponse.on("data", function (chunk) {
            console.log(`proxyResponse body length: ${chunk.length}`);
            // response.write(chunk, "binary");
        });
        proxyResponse.on("end", function () {
            console.log("proxyResponse ended");
            // response.end();
        })
    }).on('error', (err) => {
        //
        response.writeHead(500, { "content-type": 'text/html' });
        response.write("<h1>500 Server Error</h1>");
        response.write(`<h1>proxy error: ${err.message}</h1>`);
        response.end();
    });

    request.pipe(proxyRequest);

    request.on("data", function (chunk) {
        console.log(`original request body length: ${chunk.length}`);
        //  proxyRequest.write(chunk, "binary");
    });
    request.on("end", function () {
        console.log("original request ended");
        //   proxyRequest.end();
    });
}

// https代理
function httpsProxyService(request, socket) {
    // CONNECT www.hjkalhfka.jkhfk.com:8900 HTTP/1.1
    // User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0
    // Proxy-Connection: keep-alive
    // Connection: keep-alive
    // Host: www.hjkalhfka.jkhfk.com:8900

    const requestUrl = url.parse('https://' + request.url);
    const options = {
        host: requestUrl.hostname || request.headers.host.split(':')[0],
        port: requestUrl.port || request.headers.host.split(':')[1]
    };
    console.log(`------httpsProxyService-----${JSON.stringify(options)}`);
    if (options.hostname == '127.0.0.1' || options.hostname == 'localhost' || request.socket.remoteAddress.indexOf(fakeIP) != -1) {
        console.log("httpsProxyService--禁止代理访问自己");
        socket.end();
        return;
    }
    const proxySocket = net.connect(requestUrl.port, requestUrl.hostname, function () {
        // 代理接收到客户端发来'CONNECT'后，代理再与目标端建立tcp连接后，代理要回复客户端'HTTP/1.1 200'表示隧道打通
        socket.write('HTTP/1.1 200 Connection Established\r\n\r\n');
        proxySocket.pipe(socket);
    }).on('error', function (err) {
        socket.end();
    }).on('close', function (hadError) {
        if (hadError) {

        }
    });

    socket.pipe(proxySocket);
}

module.exports = {
    httpProxyService,
    httpsProxyService
}


