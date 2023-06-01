---
layout: post
title: tomcat对http connect请求的处理
subtitle: tomcat不支持http connect请求，但可以利用其升级协议机制，换新processor，尝试实现http connect隧道代理
tags: [EinkAppStore]
---
# 基础知识
## 关于 HTTP CONNECT
HTTP CONNECT 方法可以开启与所请求资源之间的双向沟通的通道。它可以用来创建隧道（tunnel）。  

例如，CONNECT 可以用来访问采用了 SSL（HTTPS）协议的站点。客户端要求 HTTP 代理服务器将 TCP 连接作为通往目的主机的隧道。之后该服务器会代替客户端与目的主机建立连接。连接建立好之后，代理服务器会面向客户端发送或接收 TCP 数据流。  

CONNECT 是一个逐跳（hop-by-hop）的方法。  


## HTTP 请求方法
根据 HTTP 标准，HTTP 请求可以使用多种请求方法（有时也叫“动作”），来表明对 Request-URL 指定的资源不同的操作方式。 

HTTP1.0 定义了三种请求方法： GET, POST 和 HEAD 方法。  
HTTP1.1 新增了六种请求方法：OPTIONS、PUT、PATCH、DELETE、TRACE 和 CONNECT 方法。  

| 序号 | 方法    | 描述                                                                   |
| --- | ------  | ---                                                                    |
| 1	  | GET	    | 请求指定的页面信息，并返回实体主体。                                       |
| 2	  | HEAD	| 类似于 GET 请求，只不过返回的响应中没有具体的内容，用于获取报头。            |
| 3	  | POST	| 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据被包含在请求体中。POST 请求可能会导致新的资源的建立和/或已有资源的修改。 |
| 4	  | PUT	    | 从客户端向服务器传送的数据取代指定的文档的内容。                             |
| 5	  | DELETE	| 请求服务器删除指定的页面。                                                 |
| 6	  | CONNECT	| HTTP/1.1 协议中预留给能够将连接改为管道方式的代理服务器。                    |
| 7	  | OPTIONS	| 允许客户端查看服务器的性能。                                               |
| 8   | TRACE	| 回显服务器收到的请求，主要用于测试或诊断。                                   |
| 9	  | PATCH	| 是对 PUT 方法的补充，用来对已知资源进行局部更新 。                            |


# Tomcat 对 HTTP CONNECT 请求的处理
> 版本： Tomcat 10.1  

## 1. 首先发送 http connect 请求看看
> Tomcat 属于 servlet 容器，servlet 规范里面没有支持 http connect 请求方法，支持 get、post、put、delete、head、options、trace 这些 method。  
 
### (1). 向 tomcat 发送 http connect 请求：  
```
CONNECT www.hjkalhfka.jkhfk.com:8900 HTTP/1.1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0
Proxy-Connection: keep-alive
Connection: keep-alive
Host: www.hjkalhfka.jkhfk.com:8900

```

### (2). tomcat 响应了501：  
```
HTTP/1.1 501
Content-Type: text/html;charset=utf-8
Content-Language: en
Content-Length: 443
Date: Wed, 17 May 2023 06:15:10 GMT
Connection: close

        <!doctype html><html lang="en"><head><title>HTTP Status 501 鈥?Not Implemented</title><style type="text/css">body {font-family:Tahoma,Arial,sans-serif;} h1, h2, h3, b {color:white;background-color:#525D76;} h1 {font-size:22px;} h2 {font-size:16px;} h3 {font-size:14px;} p {font-size:12px;} a {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style></head><body><h1>HTTP Status 501 鈥?Not Implemented</h1></body></html>

```


## 2. 再来看一下源码，是在哪儿设置响应501的   
> 涉及协议处理离不开 coyote 这个名字  

### (1). org.apache.catalina.connector.CoyoteAdapter#service  
![tomcat对http connect请求的处理](../assets/downloads/tomcat%E5%AF%B9http%20connect%E7%9A%84%E5%A4%84%E7%90%86.png)

### (2). org.apache.catalina.connector.CoyoteAdapter#postParseRequest  
![tomcat对http connect请求的处理1](../assets/downloads/tomcat%E5%AF%B9http%20connect%E7%9A%84%E5%A4%84%E7%90%861.png)
![tomcat对http connect请求的处理2](../assets/downloads/tomcat%E5%AF%B9http%20connect%E7%9A%84%E5%A4%84%E7%90%862.png)

可以看到 Tomcat 是在方法 org.apache.catalina.connector.CoyoteAdapter#postParseRequest 中对 CONNECT 作判断处理，响应了状态码501。  
```java
// CoyoteAdapter

        // Filter CONNECT method
        if (req.method().equalsIgnoreCase("CONNECT")) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, sm.getString("coyoteAdapter.connect"));
        } else {
```
```java
// HttpServletResponse

    /**
     * Status code (501) indicating the HTTP server does not support the functionality needed to fulfill the request.
     */
    int SC_NOT_IMPLEMENTED = 501;
```


# Tomcat 对升级协议的处理
> HTTP2、WebSocket  

![tomcat对http1.1协议升级的处理.png](../assets/downloads/tomcat%E5%AF%B9http1.1%E5%8D%8F%E8%AE%AE%E5%8D%87%E7%BA%A7%E7%9A%84%E5%A4%84%E7%90%86.png)
![tomcat对http1.1协议升级的处理-升级协议后会一直走dispatch().png](../assets/downloads/tomcat%E5%AF%B9http1.1%E5%8D%8F%E8%AE%AE%E5%8D%87%E7%BA%A7%E7%9A%84%E5%A4%84%E7%90%86-%E5%8D%87%E7%BA%A7%E5%8D%8F%E8%AE%AE%E5%90%8E%E4%BC%9A%E4%B8%80%E7%9B%B4%E8%B5%B0dispatch().png)

HTTP2 是在当前连接器中  
![tomcat对http1.1协议升级的处理-HTTP2.png](../assets/downloads/tomcat%E5%AF%B9http1.1%E5%8D%8F%E8%AE%AE%E5%8D%87%E7%BA%A7%E7%9A%84%E5%A4%84%E7%90%86-HTTP2.png)

WebSocket 是在 WsFilter 中  
![tomcat对http1.1协议升级的处理-WebSocket.png](../assets/downloads/tomcat%E5%AF%B9http1.1%E5%8D%8F%E8%AE%AE%E5%8D%87%E7%BA%A7%E7%9A%84%E5%A4%84%E7%90%86-WebSocket.png)

建立 WebSocket 连接的报文
```
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: nRu4KAPUPjjWYrnzxDVeqOxCvlM=

```

```
GET ws://websocket.example.com/ HTTP/1.1
Host: websocket.example.com
Upgrade: websocket
Connection: Upgrade
Origin: http://example.com
Sec-WebSocket-Key:pAloKxsGSHtpIHrJdWLvzQ==
Sec-WebSocket-Version:13

```

spring boot 允许添加 tomcat connector
![springboot允许添加自定义tomcat connector.png](../assets/downloads/springboot%E5%85%81%E8%AE%B8%E6%B7%BB%E5%8A%A0%E8%87%AA%E5%AE%9A%E4%B9%89tomcat%20connector.png)





