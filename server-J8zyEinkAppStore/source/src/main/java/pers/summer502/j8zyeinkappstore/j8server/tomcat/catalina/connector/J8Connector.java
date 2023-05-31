package pers.summer502.j8zyeinkappstore.j8server.tomcat.catalina.connector;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.CoyoteAdapter;
import org.apache.catalina.core.AprStatus;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;

import javax.management.ObjectName;

/**
 * connector
 *
 * @author summer502
 */
public class J8Connector extends Connector {
    public J8Connector() {
        super("HTTP/1.1");
    }

    public J8Connector(String protocol) {
        super(protocol);
    }

    public J8Connector(ProtocolHandler protocolHandler) {
        super(protocolHandler);
    }

    //Wrapper
    @Override
    protected void initInternal() throws LifecycleException {

        // super.initInternal();
        // ---new1--改-------------------------------------------------------
        ObjectName oname = getObjectName();
        if (oname == null) {
            oname = register(this, getObjectNameKeyProperties());
            try {
                preRegister(null, oname);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ---new1--改-------------------------------------------------------

        if (protocolHandler == null) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInstantiationFailed"));
        }

        // Initialize adapter
        // adapter = new CoyoteAdapter(this);
        // ---new2--改-------------------------------------------------------
        adapter = new J8CoyoteAdapterWrapper(new CoyoteAdapter(this));
        // ---new2--改-------------------------------------------------------
        protocolHandler.setAdapter(adapter);
        if (service != null) {
            protocolHandler.setUtilityExecutor(service.getServer().getUtilityExecutor());
        }

        // Make sure parseBodyMethodsSet has a default
        if (null == parseBodyMethodsSet) {
            setParseBodyMethods(getParseBodyMethods());
        }

        if (AprStatus.isAprAvailable() && AprStatus.getUseOpenSSL() &&
                protocolHandler instanceof AbstractHttp11JsseProtocol) {
            AbstractHttp11JsseProtocol<?> jsseProtocolHandler = (AbstractHttp11JsseProtocol<?>) protocolHandler;
            if (jsseProtocolHandler.isSSLEnabled() && jsseProtocolHandler.getSslImplementationName() == null) {
                // OpenSSL is compatible with the JSSE configuration, so use it if APR is available
                jsseProtocolHandler.setSslImplementationName(OpenSSLImplementation.class.getName());
            }
        }

        try {
            protocolHandler.init();
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInitializationFailed"), e);
        }
    }

    @Override
    public String toString() {
        // Not worth caching this right now
        StringBuilder sb = new StringBuilder("J8Connector[");
        sb.append(getProtocol());
        sb.append('-');
        String id = (protocolHandler != null) ? protocolHandler.getId() : null;
        if (id != null) {
            sb.append(id);
        } else {
            int port = getPortWithOffset();
            if (port > 0) {
                sb.append(port);
            } else {
                sb.append("auto-");
                sb.append(getProperty("nameIndex"));
            }
        }
        sb.append(']');
        return sb.toString();
    }


}
