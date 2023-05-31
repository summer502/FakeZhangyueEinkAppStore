package pers.summer502.j8zyeinkappstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pers.summer502.j8zyeinkappstore.model.ServerInfoDTO;
import pers.summer502.j8zyeinkappstore.model.ServerLogDTO;
import pers.summer502.j8zyeinkappstore.service.ServerStatusService;
import pers.summer502.j8zyeinkappstore.util.ResponseBodyResult;
import pers.summer502.j8zyeinkappstore.util.SseEmitterSupport;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServerStatusServiceImpl implements ServerStatusService {
    private final Logger logger = LoggerFactory.getLogger(ServerStatusServiceImpl.class);

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String osName = System.getProperty("os.name");

    @Override
    public ServerInfoDTO getServerInfo() {
        ServerInfoDTO serverInfoDTO = new ServerInfoDTO();
        serverInfoDTO.setDate(dtf.format(LocalDateTime.now()));
        serverInfoDTO.setPlatform(osName);
        serverInfoDTO.setConnections(counter.get());
        serverInfoDTO.setRequests(0);
        serverInfoDTO.setFreeMemory(0);
        serverInfoDTO.setTotalMemory(0);

        return serverInfoDTO;
    }

    @Override
    public ServerLogDTO getServerLog(String lastlogid) {
        ServerLogDTO serverLogDTO = new ServerLogDTO();
        if (lastlogid == null) {
            // 说明是第一次连接，推送最新的一条日志
            serverLogDTO.addLogs("1", "test1--------");
        } else {
            // 推送从 lastlogid 以后的日志，不包括 lastlogid
            serverLogDTO.addLogs("10", "test----10");
            serverLogDTO.addLogs("11", "test----11");
            serverLogDTO.addLogs("12", "test----12");
        }
        return serverLogDTO;
    }

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public SseEmitter startSseEmitter(String clientId) {
        SseEmitter sseEmitter = SseEmitterSupport.startSseEmitter(clientId, 0L);
        sseEmitter.onTimeout(() -> {
            logger.info("startSseEmitter, onTimeout, clientId={}, Thread={}", clientId, Thread.currentThread().getName());
            sseEmitter.complete();
        });
        sseEmitter.onError(throwable -> {
            logger.info("startSseEmitter, onError, clientId={}, error={}, Thread={}", clientId, throwable.getMessage(), Thread.currentThread().getName());
            sseEmitter.complete();
        });
        sseEmitter.onCompletion(() -> {
            logger.info("startSseEmitter, onCompletion, clientId={}, Thread={}", clientId, Thread.currentThread().getName());// Thread=http-nio-80-exec-2
            counter.getAndDecrement();
        });
        counter.getAndIncrement();
        return sseEmitter;
    }

    @Override
    public void sendConnectTime(SseEmitter sseEmitter) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event()
                .name("connect-time")// 自定义事件 connect-time
                .id("888888")
                .reconnectTime(30 * 1000L)
                .data("---" + dtf.format(LocalDateTime.now()) + "---")
                .comment("This is a comment");
        try {
            sseEmitter.send(builder);
        } catch (IOException e) {
            logger.error("sendConnectTime, error={}", e.getMessage());
            sseEmitter.completeWithError(e);
        }
    }

    @Override
    public void sendConnectClose(SseEmitter sseEmitter) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event()
                .name("connect-close"); // 自定义事件 connect-close
        try {
            sseEmitter.send(builder);
        } catch (IOException e) {
            logger.error("sendConnectClose, error={}", e.getMessage());
            sseEmitter.completeWithError(e);
        }
    }

    @Override
    public void sendServerInfo() {
        ServerInfoDTO serverInfo = this.getServerInfo();

        SseEmitter.SseEventBuilder builder = SseEmitter.event()
                .name("server-info")// 自定义事件 server-info
                .reconnectTime(5 * 1000L)
                .data(ResponseBodyResult.success(serverInfo), MediaType.APPLICATION_JSON);

        int i = SseEmitterSupport.sendMessageToAllClient(builder);
        logger.debug("sendServerInfo, 向{}个客户端推送了server-info", i);
    }

    @Override
    public void sendServerLog(String lastEventId) {
        ServerLogDTO serverLog = this.getServerLog(lastEventId);
        String lastlogid = serverLog.getLastlogid();

        SseEmitter.SseEventBuilder builder = SseEmitter.event()
                .name("server-log")// 自定义事件 server-log
                .id(lastlogid)// lastEventId
                .reconnectTime(30 * 1000L)
                .data(ResponseBodyResult.success(serverLog), MediaType.APPLICATION_JSON);

        int i = SseEmitterSupport.sendMessageToAllClient(builder);
        logger.debug("sendServerLog, 向{}个客户端推送了server-log", i);
    }

}
