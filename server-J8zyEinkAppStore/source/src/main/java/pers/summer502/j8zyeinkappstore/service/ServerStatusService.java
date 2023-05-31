package pers.summer502.j8zyeinkappstore.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pers.summer502.j8zyeinkappstore.model.ServerInfoDTO;
import pers.summer502.j8zyeinkappstore.model.ServerLogDTO;

public interface ServerStatusService {
    ServerInfoDTO getServerInfo();

    ServerLogDTO getServerLog(String lastlogid);

    SseEmitter startSseEmitter(String clientId);

    void sendConnectTime(SseEmitter sseEmitter);

    void sendConnectClose(SseEmitter sseEmitter);

    void sendServerInfo();

    void sendServerLog(String lastEventId);
}
