package pers.summer502.j8zyeinkappstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 伪掌阅iReader应用商店服务端，端口80
 * 有2种访问方式：
 * 1、拦截域名"ebook.zhangyue.com"，使用http get访问此80端口，此时只能访问"ebook.zhangyue.com:80"下的接口地址。反向代理模式。
 * 2、配置HTTP代理服务器，指定此80端口。正向代理模式。
 *
 * @author summer502
 */
@SpringBootApplication
public class FakeZhangyueEinkAppStoreApplication {
    private static final Logger logger = LoggerFactory.getLogger(FakeZhangyueEinkAppStoreApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FakeZhangyueEinkAppStoreApplication.class, args);

        logger.info("-------------------------------------------------------");
        logger.info("访问页面地址：http://127.0.0.1:80/EinkAppStore/");
        logger.info("-------------------------------------------------------");
    }
}