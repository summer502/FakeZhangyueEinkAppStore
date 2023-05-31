package pers.summer502.j8zyeinkappstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pers.summer502.j8zyeinkappstore.model.ServerInfoDTO;
import pers.summer502.j8zyeinkappstore.model.ServerLogDTO;
import pers.summer502.j8zyeinkappstore.service.ServerStatusService;
import pers.summer502.j8zyeinkappstore.util.ResponseBodyResult;

import java.io.IOException;
import java.util.Date;

/**
 * 向客户端推送消息
 * http://127.0.0.1:80/EinkAppStore/server-status/sse
 * http://127.0.0.1:80/EinkAppStore/server-status/ajax/info
 * http://127.0.0.1:80/EinkAppStore/server-status/ajax/log?lastlogid=0
 *
 * @author summer502
 */
@Controller
@RequestMapping("/EinkAppStore")
public class ServerStatusController {
    private final Logger logger = LoggerFactory.getLogger(ServerStatusController.class);

    @Autowired
    private ServerStatusService service;

    private final SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("ServerStatus-");

    @GetMapping("/server-status/sse")
    public SseEmitter sse(String clientId,
                          @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        logger.info("sse, clientId={}, lastEventId={}, Thread={}", clientId, lastEventId, Thread.currentThread().getName());

        SseEmitter sseEmitter = service.startSseEmitter(sessionId);

        // 默认事件
        try {
            sseEmitter.send("test------" + new Date());
        } catch (IOException e) {
            logger.info("sse, error={}, Thread={}", e.getMessage(), Thread.currentThread().getName());
        }

        // 自定义事件 connect-time
        service.sendConnectTime(sseEmitter);

        // 自定义事件 server-log
        service.sendServerLog(lastEventId);

        return sseEmitter;
    }

    @GetMapping("/server-status/ajax/{flag}")
    @ResponseBody
    public DeferredResult<ResponseBodyResult<Object>> ajax(@PathVariable String flag,
                                                           String clientId,
                                                           @RequestParam(value = "lastlogid", required = false) String lastlogid) {
        DeferredResult<ResponseBodyResult<Object>> deferredResult = new DeferredResult<>();
        if ("log".equals(flag)) {
            // server-log
            Runnable task = () -> {
                ServerLogDTO serverLog = service.getServerLog(lastlogid);

                deferredResult.setResult(ResponseBodyResult.success(serverLog));
            };
            asyncTaskExecutor.execute(task);
        } else if ("info".equals(flag)) {
            // server-info
            Runnable task = () -> {
                ServerInfoDTO serverInfo = service.getServerInfo();

                deferredResult.setResult(ResponseBodyResult.success(serverInfo));
            };
            asyncTaskExecutor.execute(task);
        } else {
            // 未知 flag
            deferredResult.setResult(ResponseBodyResult.error("未知 flag"));
        }

        return deferredResult;
    }


}
