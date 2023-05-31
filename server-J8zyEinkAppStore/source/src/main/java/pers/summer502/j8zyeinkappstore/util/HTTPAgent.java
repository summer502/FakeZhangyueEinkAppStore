package pers.summer502.j8zyeinkappstore.util;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import pers.summer502.j8zyeinkappstore.controller.EinkAppStoreController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

public class HTTPAgent implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(HTTPAgent.class);

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    private RequestConfig requestConfig = RequestConfig.custom()
            .setResponseTimeout(30 * 1000, TimeUnit.MILLISECONDS)// 响应超时时间
            .setConnectionRequestTimeout(30 * 1000, TimeUnit.MILLISECONDS) // 从连接池中获取连接的超时时间
            .build();
    private CloseableHttpClient httpClient1 = HttpClients.custom()
            .setDefaultHeaders(Collections.emptyList())
            .setDefaultRequestConfig(requestConfig)
            .build();

    /**
     * 获得客户端真实IP地址
     *
     * @param request
     * @return
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private int getPort(String[] hostInfo, String scheme) {
        String port = null;
        if (hostInfo.length > 1) {
            port = hostInfo[1];
        }
        if (port == null || port.isBlank()) {
            if ("http".equalsIgnoreCase(scheme)) {
                return 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                return 443;
            } else {
                return 80;
            }
        } else {
            return Integer.parseInt(port);
        }
    }

    // GET http://www.hjkalhfka.jkhfk.com:8900/ljlioiikkk/path?sdfkah=jklasdhfk HTTP/1.1
    // Host: www.hjkalhfka.jkhfk.com:8900
    // User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0
    // Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8
    // Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
    // Accept-Encoding: gzip, deflate
    // Connection: keep-alive
    // Upgrade-Insecure-Requests: 1
    private boolean doTask(HttpServletRequest clientRequest, HttpServletResponse clientResponse) throws Exception {
        String protocol = clientRequest.getProtocol();
        String method = clientRequest.getMethod();

        String scheme = clientRequest.getScheme();
        String serverName = clientRequest.getServerName();// 服务器的名称
        int serverPort = clientRequest.getServerPort();// 服务器的端口
        String requestURI = clientRequest.getRequestURI();// 服务器的资源路径

        String requestURL = clientRequest.getRequestURL().toString();
        String queryString = clientRequest.getQueryString();

        String remoteHost = clientRequest.getRemoteHost();// 客户端的主机名
        String remoteAddr = clientRequest.getRemoteAddr();// 客户端的ip地址
        int remotePort = clientRequest.getRemotePort();
        String localName = clientRequest.getLocalName();// 服务端主机名
        String localAddr = clientRequest.getLocalAddr();// 服务器本机IP地址
        int localPort = clientRequest.getLocalPort();

        Enumeration<String> headerNames = clientRequest.getHeaderNames();
        String contentType = clientRequest.getContentType();
        String characterEncoding = clientRequest.getCharacterEncoding();
        long contentLengthLong = clientRequest.getContentLengthLong();
        ServletInputStream clientInputStream = clientRequest.getInputStream();
        ServletOutputStream clientOutputStream = clientResponse.getOutputStream();

        // ------------------------------------------------------------ ip, port
        String ip;
        int port;
        String host = clientRequest.getHeader("HOST");
        if (host == null || host.isBlank()) {
            ip = serverName;
            port = serverPort;
        } else {
            String[] hostInfo = host.split(":");
            ip = hostInfo[0];
            port = getPort(hostInfo, scheme);
        }
        String url = requestURL;
        if (queryString != null && !queryString.isBlank()) {
            url = url + "?" + queryString;
        }
        logger.info("HTTP, ip={}, port={}, protocol={}, url={}", ip, port, protocol, url);

        // ------------------------------------------------------------ 禁止代理自己访问自己
        if (("127.0.0.1".equals(ip) || "localhost".equals(ip) || localAddr.equals(ip)) && (port == localPort)) {
            // 此时已经明确是在请求访问本机的资源，访问本机的这些资源路径时，要放行
            boolean matchResult = antPathMatcher.match("/favicon.ico", requestURI);
            if (matchResult) {
                return true;
            }
            matchResult = antPathMatcher.match("/images/*", requestURI);
            if (matchResult) {
                return true;
            }
            matchResult = antPathMatcher.match("/error/**", requestURI);
            if (matchResult) {
                return true;
            }
            matchResult = antPathMatcher.match("/error", requestURI);
            if (matchResult) {
                return true;
            }

            logger.error("出现对环回地址的调用。不允许代理自己访问自己。");
            // 不允许
            return false;
        }

        // ------------------------------------------------------------ httpClient 请求
        // ClassicHttpRequest
        HttpUriRequestBase httpUriRequest = new HttpUriRequestBase(method, URI.create(url));
        httpUriRequest.setConfig(requestConfig);

        String[] prover = protocol.split("/");
        String[] ver = prover[1].split("\\.");
        ProtocolVersion protocolVersion = new ProtocolVersion(prover[0], Integer.parseInt(ver[0]), Integer.parseInt(ver[1]));
        httpUriRequest.setVersion(protocolVersion);

        ContentType content_type = null;
        if (contentType != null) {
            content_type = ContentType.create(contentType, characterEncoding);
        }
        if (content_type == null) {
            content_type = ContentType.parse(contentType);
            if (content_type == null) {
                content_type = ContentType.parseLenient(contentType);
            }
        }
        BasicHttpEntity httpEntity = new BasicHttpEntity(clientInputStream, contentLengthLong, content_type);
//        byte[] body = StreamUtils.copyToByteArray(clientInputStream);
//        ByteArrayEntity basicHttpEntityd1 = new ByteArrayEntity(body, content_type);
        httpUriRequest.setEntity(httpEntity);

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = clientRequest.getHeader(name);

            if ("content-length".equalsIgnoreCase(name)) {
                continue;
            }
            httpUriRequest.setHeader(name, value);
        }

        // HttpHost
        HttpHost httpHost = new HttpHost(scheme, ip, port);

        // HttpClientResponseHandler
        HttpClientResponseHandler<byte[]> httpClientResponseHandler = new HttpClientResponseHandler<>() {
            @Override
            public byte[] handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                int code = response.getCode();
                Header[] headers = response.getHeaders();
                HttpEntity entity = response.getEntity();

                clientResponse.setStatus(code);
                for (Header header : headers) {
                    clientResponse.addHeader(header.getName(), header.getValue());
                }

                byte[] httpResponseBody = EntityUtils.toByteArray(entity);
                if (httpResponseBody != null) {
                    clientOutputStream.write(httpResponseBody);
                    clientOutputStream.flush();
                }
                if (code >= HttpStatus.SC_REDIRECTION) {
                    String reasonPhrase = response.getReasonPhrase();
                    logger.warn("HttpClient execute, httpResponseCode={}, reasonPhrase={}", code, reasonPhrase);
                }
                Closer.close(entity);
                return httpResponseBody;
            }
        };

        try {
            httpClient.execute(httpHost, httpUriRequest, httpClientResponseHandler);
        } catch (IOException e) {
            logger.error("HttpClient execute, error={}", e.getMessage());
            clientResponse.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "HttpClient, error");
        } finally {
            Closer.close(httpEntity);
            //      Closer.close(basicHttpEntityd1);
            //   Closer.close(httpClient);
        }

        // 不允许
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("handler: " + handler);
//        System.out.println("Request URL: " + request.getRequestURL());
//        handler: ResourceHttpRequestHandler [classpath [META-INF/resources/], classpath [resources/], classpath [static/], classpath [public/], ServletContext [/]]
//        Request URL: http://ebook.zhangyue.com/zybook/u/p/api.php
        loglog(request);

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> beanType = handlerMethod.getBeanType();
            // EinkAppStoreController
            if (EinkAppStoreController.class.equals(beanType)) {
                Method method = handlerMethod.getMethod();
                String name = method.getName();
                if ("getAppList".equals(name) || "getAppInfo".equals(name) || "getCategory".equals(name)) {
                    long startTime = System.currentTimeMillis();
                    request.setAttribute("startTime", startTime);
                    return true;
                } else if ("other".equals(name)) {
                    // 走 HTTP 代理
                    // return doTask(request, response);
                }
            }
        }

        return doTask(request, response);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        String requestURI = request.getRequestURI();
//        logger.info("requestURI={}", requestURI);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> beanType = handlerMethod.getBeanType();
            // EinkAppStoreController
            if (EinkAppStoreController.class.equals(beanType)) {
                Method method = handlerMethod.getMethod();
                String name = method.getName();
                if ("getAppList".equals(name) || "getAppInfo".equals(name) || "getCategory".equals(name)) {
                    long startTime = (Long) request.getAttribute("startTime");
                    long endTime = System.currentTimeMillis();
                    logger.info("访问{}总耗时={}毫秒", name, (endTime - startTime));
                }
            }
        }

    }

    private void loglog(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String user_agent = request.getHeader("user-agent");
        logger.info("{} --- [{}] \"{} {}\" \"{}\"", getClientIpAddr(request), startTime, method, requestURI, user_agent);
    }
}
