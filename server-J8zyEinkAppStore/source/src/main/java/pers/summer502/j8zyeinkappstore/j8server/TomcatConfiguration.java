package pers.summer502.j8zyeinkappstore.j8server;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.summer502.j8zyeinkappstore.j8server.tomcat.catalina.connector.J8Connector;

/**
 * 自定义 tomcat
 *
 * @author summer502
 */
@Configuration(proxyBeanMethods = false)
public class TomcatConfiguration {
    private final Logger logger = LoggerFactory.getLogger(TomcatConfiguration.class);

    @Value("${server.tomcat.port}")
    private int port;

    /**
     * 自定义 tomcat connector
     *
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorCustomizer() {
        return (factory) -> factory.addAdditionalTomcatConnectors(this.createJ8Connector());
    }

    private Connector createJ8Connector() {
        Connector connector = new J8Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(port);
        return connector;
    }

}
