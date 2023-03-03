const EventEmitter = require('events');

const sseEmitter = new EventEmitter();
const keepAlive = 60 * 1000;

// 订阅
function subscribe(request, response) {
    // 响应头
    response.writeHead(200, {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-transform",
        "Connection": "keep-alive",
        "X-Accel-Buffering": "no"
    });

    // 建立连接后，发送connectTime
    response.write(": This is a comment\n");
    response.write("retry: 10000\n");
    response.write("event: connectTime\n");// 自定义事件 connectTime
    response.write("id: 0\n");
    response.write("data: " + (new Date()) + "\n\n");
    // 消息格式："field:value\n\n"。其中"field"有5类值：""、"retry"、"event"、"id"、"data"。
    // 用\n来分隔每一行数据，用\n\n来分隔每一个事件。
    // 每一个事件中包含事件的type和事件的data，分别用两行来描述。
    // 上面是返回来一个"connectTime"事件，对应的数据行应该是"event: connectTime\n"。
    // 下面是返回来一个"message"事件（若不指定事件类型，则默认"message"）。
    response.write("data: " + (new Date().toUTCString()) + "\n\n");
    response.write("data: " + (new Date().toISOString()) + "\n\n");

    // pushData
    const pushData = function (comment, retry, eventType, eventId, eventData) {
        if (comment) {
            response.write(': ' + comment + '\n');
        }
        if (retry) {
            response.write("retry: " + retry + "\n");
        }
        if (eventType) {
            response.write('event: ' + eventType + '\n');
        }
        if (eventId) {
            response.write("id: " + eventId + "\n");
        }
        response.write('data: ' + eventData + '\n\n');
    };

    // Heartbeat
    const pushHeartbeat = function () {
        response.write(':\n\n');
    };

    // 注册事件监听器
    // sseEmitter.on('sse-message', (eventData) => {
    //     response.write('data: ' + eventData + '\n\n');
    // });
    sseEmitter.on('sse-pushdata', pushData);

    // 启动心跳
    const heartbeatTimer = setInterval(pushHeartbeat, keepAlive);

    // 绑定事件
    response.on('close', function () {
        clearInterval(heartbeatTimer);
        sseEmitter.removeListener('sse-pushdata', pushData);
    });
    response.on('error', function (err) {
        console.error('sse response error: ', err);
    });
}

// 发布
function publish(comment, retry, eventType, eventId, eventData) {
    sseEmitter.emit('sse-pushdata', comment, retry, eventType, eventId, eventData);
}

module.exports = {
    subscribe,
    publish
}
