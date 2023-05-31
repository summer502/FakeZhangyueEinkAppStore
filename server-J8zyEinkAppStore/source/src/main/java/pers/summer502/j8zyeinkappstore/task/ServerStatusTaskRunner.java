package pers.summer502.j8zyeinkappstore.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pers.summer502.j8zyeinkappstore.service.ServerStatusService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ServerStatusTaskRunner implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(ServerStatusTaskRunner.class);

    @Autowired
    private ServerStatusService serverStatusService;

    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        startSendServerStatus();
    }

    ScheduledFuture<?> sendServerStatusFuture;

    private void startSendServerStatus() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 自定义事件 server-info
                serverStatusService.sendServerInfo();
                // 自定义事件 server-log
                //serverStatusService.sendServerLog();
            }
        };
        sendServerStatusFuture = scheduledExecutor.scheduleWithFixedDelay(task, 5, 1, TimeUnit.SECONDS);
        logger.info("sendServerStatusToAllClient start, 每秒向所有客户端推送一次");
    }

    public void stopSendServerStatus() {
        sendServerStatusFuture.cancel(true);
        logger.info("sendServerStatusToAllClient stop");
    }
}
