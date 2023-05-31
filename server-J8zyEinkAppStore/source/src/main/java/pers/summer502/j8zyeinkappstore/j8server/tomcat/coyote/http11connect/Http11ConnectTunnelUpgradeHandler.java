package pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;
import org.apache.coyote.http11.upgrade.UpgradeProcessorExternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * HTTP Connect Tunnel
 * 对应的 processor 使用 {@link UpgradeProcessorExternal}
 */
public class Http11ConnectTunnelUpgradeHandler implements HttpUpgradeHandler {
    private final Logger logger = LoggerFactory.getLogger(Http11ConnectTunnelUpgradeHandler.class);

    private WebConnection connection;

    private SocketChannel clientAgent;

    private final ByteBuffer requestDataBuffer = ByteBuffer.allocate(8 * 1024);

    private final ByteBuffer responseDataBuffer = ByteBuffer.allocate(8 * 1024);

    public Http11ConnectTunnelUpgradeHandler(SocketChannel clientAgent) {
        this.clientAgent = clientAgent;
    }

    @Override
    public void init(WebConnection connection) {
        if (connection == null) {
            throw new IllegalStateException("connection is null");
        }
        this.connection = connection;

        if (this.connection instanceof UpgradeProcessorExternal) {
            UpgradeProcessorExternal processor = (UpgradeProcessorExternal) connection;
            try {
                if (!this.clientAgent.finishConnect()) {
                    throw new IllegalStateException("clientAgent is not connected");
                }

                // UpgradeServletInputStream
                ServletInputStream inputStream = this.connection.getInputStream();
                // UpgradeServletOutputStream
                ServletOutputStream outputStream = this.connection.getOutputStream();

                inputStream.setReadListener(new ConnectTunnelReadListener(this));
//                outputStream.setWriteListener(new ConnectTunnelWriteListener(this));
            } catch (IOException e) {
                this.destroy();
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException("not UpgradeProcessorExternal");
        }
    }

    @Override
    public void destroy() {
        WebConnection connection = this.connection;
        SocketChannel clientAgent = this.clientAgent;
        try {
            if (clientAgent != null) {
                this.clientAgent = null;
                clientAgent.close();
            }
            if (connection != null) {
                this.connection = null;
                connection.close();
            }
        } catch (Exception e) {
            logger.error("destroy, error={}", e.getMessage());
        }
    }

    /**
     * readClientRequest --> writeServerRequest
     * {@link ConnectTunnelReadListener}
     *
     * @throws IOException
     */
    public void readClientRequestAndWriteServerRequest() throws IOException {
        if (!this.clientAgent.finishConnect()) {
            throw new IllegalStateException("clientAgent is not connected");
        }

        ServletInputStream inputStream = connection.getInputStream();
        if (clientAgent.isConnected()) {
            byte[] requestDataBuf = requestDataBuffer.array();
            int len;
            while (inputStream.isReady()) {
                // readClientRequest
                len = inputStream.read(requestDataBuf);

                requestDataBuffer.position(len);
                requestDataBuffer.flip();

                while (requestDataBuffer.hasRemaining()) {
                    // writeServerRequest
                    int write = clientAgent.write(requestDataBuffer);
                    logger.info("ConnectTunnelReadListener, readClientRequest={}, writeServerRequest={}", len, write);
                }
                requestDataBuffer.clear();
            }

            // 读完客户端的数据后，开始向客户端写数据
            ServletOutputStream outputStream = connection.getOutputStream();
            outputStream.setWriteListener(new ConnectTunnelWriteListener(this));
        } else {
            destroy();
        }
    }

    /**
     * readServerResponse --> writeClientResponse
     * {@link ConnectTunnelWriteListener}
     *
     * @throws IOException
     */
    public void readServerResponseAndWriteClientResponse() throws IOException {
        if (!this.clientAgent.finishConnect()) {
            throw new IllegalStateException("clientAgent is not connected");
        }

        ServletOutputStream outputStream = connection.getOutputStream();
        if (clientAgent.isConnected()) {
            if (outputStream.isReady()) {
                byte[] responseDataBuf = responseDataBuffer.array();
                int len;
                // readServerResponse
                // 当 len=-1 或 len=0 时，执行 destroy() 方法
                // 注意，这个 clientAgent 是阻塞的
                while ((len = clientAgent.read(responseDataBuffer)) > 0) {
                    responseDataBuffer.flip();

                    // writeClientResponse
                    outputStream.write(responseDataBuf, 0, responseDataBuffer.limit());
                    logger.info("ConnectTunnelWriteListener, readServerResponse={}, writeClientResponse={}", len, responseDataBuffer.limit());
                    outputStream.flush();

                    responseDataBuffer.clear();
                }
                // 执行 destroy() 方法
                destroy();
            }
        } else {
            destroy();
        }
    }
}
