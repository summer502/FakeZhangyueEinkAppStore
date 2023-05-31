package pers.summer502.j8zyeinkappstore.j8server.tomcat.catalina.connector;

import org.apache.catalina.connector.CoyoteAdapter;
import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.net.SocketEvent;
import pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect.UpgradeUtil;

/**
 * Tomcat Coyote Adapter
 * 在 service() 方法中处理 HTTP CONNECT
 *
 * @author summer502
 */
public class J8CoyoteAdapterWrapper implements Adapter {
    long timeout = 1000 * 60 * 60 * 24;

    private final CoyoteAdapter coyoteAdapter;

    public J8CoyoteAdapterWrapper(CoyoteAdapter coyoteAdapter) {
        this.coyoteAdapter = coyoteAdapter;
    }


    // CONNECT www.hjkalhfka.jkhfk.com:8900 HTTP/1.1
    // User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0
    // Proxy-Connection: keep-alive
    // Connection: keep-alive
    // Host: www.hjkalhfka.jkhfk.com:8900
    @Override
    public void service(Request req, Response res) throws Exception {
        if (req.method().equalsIgnoreCase("CONNECT")) {
            // 是 HTTP CONNECT，进行升级协议
            UpgradeUtil.doUpgrade(req, res);
        } else {
            coyoteAdapter.service(req, res);
        }
    }

    @Override
    public boolean prepare(Request req, Response res) throws Exception {
        if (req.method().equalsIgnoreCase("CONNECT")) {
            // 是 HTTP CONNECT，进行升级协议
            System.out.println("J8CoyoteAdapterWrapper, prepare(), req.method()=CONNECT" );
        }

        return coyoteAdapter.prepare(req, res);
    }

    @Override
    public boolean asyncDispatch(Request req, Response res, SocketEvent status) throws Exception {
        return coyoteAdapter.asyncDispatch(req, res, status);
    }

    @Override
    public void log(Request req, Response res, long time) {
        coyoteAdapter.log(req, res, time);
    }

    @Override
    public void checkRecycled(Request req, Response res) {
        coyoteAdapter.checkRecycled(req, res);
    }

    @Override
    public String getDomain() {
        return coyoteAdapter.getDomain();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("J8CoyoteAdapterWrapper[");
        sb.append(']');
        return sb.toString();
    }
}
