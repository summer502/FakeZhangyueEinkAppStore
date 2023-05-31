package pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect;

import jakarta.servlet.ReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConnectTunnelReadListener implements ReadListener {
    private final Logger logger = LoggerFactory.getLogger(ConnectTunnelReadListener.class);

    private final Http11ConnectTunnelUpgradeHandler upgradeHandler;

    public ConnectTunnelReadListener(Http11ConnectTunnelUpgradeHandler upgradeHandler) {
        this.upgradeHandler = upgradeHandler;
    }

    @Override
    public void onDataAvailable() throws IOException {
        upgradeHandler.readClientRequestAndWriteServerRequest();
    }

    @Override
    public void onAllDataRead() throws IOException {
        logger.info("客户端数据eof");
        upgradeHandler.destroy();
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("ReadListener onError, error={}", throwable.getMessage());
        upgradeHandler.destroy();
    }
}
