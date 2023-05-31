package pers.summer502.j8zyeinkappstore.listener;

import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;


public class HomeHtmlAsyncTaskListener implements AsyncListener {
    private final Logger logger = LoggerFactory.getLogger(HomeHtmlAsyncTaskListener.class);

    private String pathName;

    public HomeHtmlAsyncTaskListener(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        logger.info("EinkAppStore, home onComplete, pathName= {}, thread={}", pathName, Thread.currentThread().getName());// http-nio-80-exec-2
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        AsyncContext asyncContext = event.getAsyncContext();
        ServletRequest suppliedRequest = event.getSuppliedRequest();
        ServletResponse suppliedResponse = event.getSuppliedResponse();

        logger.error("EinkAppStore, home onTimeout, pathName= {}, thread={}", pathName, Thread.currentThread().getName());
        PrintWriter out = asyncContext.getResponse().getWriter();
        out.print("访问" + pathName + "超时[" + asyncContext.getTimeout() + "]毫秒");

        asyncContext.complete();
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        Throwable throwable = event.getThrowable();

        logger.error("EinkAppStore, home onError, pathName= {}, error={}, thread={}", pathName, throwable.getMessage(), Thread.currentThread().getName());
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        logger.info("EinkAppStore, home onStartAsync, pathName= {}, thread={}", pathName, Thread.currentThread().getName());
    }
}