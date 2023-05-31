package pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect;

import jakarta.servlet.WriteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConnectTunnelWriteListener implements WriteListener {
    private final Logger logger = LoggerFactory.getLogger(ConnectTunnelWriteListener.class);

    private final Http11ConnectTunnelUpgradeHandler upgradeHandler;

    public ConnectTunnelWriteListener(Http11ConnectTunnelUpgradeHandler upgradeHandler) {
        this.upgradeHandler = upgradeHandler;
    }

    @Override
    public void onWritePossible() throws IOException {
        upgradeHandler.readServerResponseAndWriteClientResponse();
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("WriteListener onError, error={}", throwable.getMessage());
        upgradeHandler.destroy();
    }
}
