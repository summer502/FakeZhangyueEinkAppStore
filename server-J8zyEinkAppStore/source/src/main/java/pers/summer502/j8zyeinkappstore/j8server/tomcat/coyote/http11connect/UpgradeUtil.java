package pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.UpgradeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class UpgradeUtil {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeUtil.class);

    private UpgradeUtil() {
    }

    public static boolean isHttpConnectUpgradeRequest(ServletRequest request, ServletResponse response) {
        return ((request instanceof HttpServletRequest)
                && (response instanceof HttpServletResponse)
                && "CONNECT".equals(((HttpServletRequest) request).getMethod()));
    }

    public static void doUpgrade(org.apache.coyote.Request req, org.apache.coyote.Response res) {
        try {
            // 代理端接收到客户端发来'CONNECT'后，代理端需解析报文中的ip和port，该ip和port是连接目标端的
            // 代理端与目标端建立tcp连接后，代理端要立即回复客户端'HTTP/1.1 200'表示隧道打通
            SocketChannel clientAgent = connectServer(req, res);
            logger.info("HTTP CONNECT, success");

            // 建立连接后，返回"HTTP/1.1 200 Connection Established\r\n\r\n"
            res.setStatus(HttpServletResponse.SC_OK);
            res.action(ActionCode.CLIENT_FLUSH, null);

            // 进行升级协议，更换 processor 为 UpgradeProcessorExternal，
            // 确保 Http11Processor.isUpgrade() 为true，
            // 使 Http11Processor.service() 返回“AbstractEndpoint.Handler.SocketState.UPGRADING”
            Http11ConnectTunnelUpgradeHandler upgradeHandler = new Http11ConnectTunnelUpgradeHandler(clientAgent);
            YummyContextBind yummyContextBind = new YummyContextBind();
            UpgradeToken upgradeToken = new UpgradeToken(upgradeHandler, yummyContextBind, null, "HTTP CONNECT");
            req.action(ActionCode.UPGRADE, upgradeToken);
        } catch (Exception e) {
            logger.info("HTTP CONNECT, error={}", e.getMessage());

            res.setError();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setMessage("connect target server error");
            res.action(ActionCode.CLIENT_FLUSH, null);
        }
    }

    private static SocketChannel connectServer(Request coyoteRequest, Response coyoteResponse) throws Exception {
        // 解析报文中的 ip 和 port
        String serverName = coyoteRequest.serverName().toString();
        int serverPort = coyoteRequest.getServerPort();

        if (serverName == null) {
            String[] hostInfo;
            String host = coyoteRequest.getHeader("HOST");
            if (host == null || host.isBlank()) {
                String requestURI = coyoteRequest.requestURI().toString();
                hostInfo = requestURI.split(":");
            } else {
                hostInfo = host.split(":");
            }

            String ip = hostInfo[0];
            String port = null;
            if (hostInfo.length > 1) {
                port = hostInfo[1];
            }
            if (port == null || port.isBlank()) {
                if ("http".equalsIgnoreCase(coyoteRequest.scheme().toString())) {
                    port = "80";
                } else if ("https".equalsIgnoreCase(coyoteRequest.scheme().toString())) {
                    port = "443";
                } else {
                    port = "80";
                }
            }
            serverName = ip;
            serverPort = Integer.parseInt(port);
        }
        logger.info("HTTP CONNECT, serverName={}, serverPort={}", serverName, serverPort);

        // 创建 socket 链接
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(serverName, serverPort));
        //  socketChannel.configureBlocking(false);
        socketChannel.socket().setSoTimeout(0);
        return socketChannel;
    }
}
