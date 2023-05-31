package pers.summer502.j8zyeinkappstore.controller;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import pers.summer502.j8zyeinkappstore.listener.HomeHtmlAsyncTaskListener;
import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;
import pers.summer502.j8zyeinkappstore.service.HomeService;

/**
 * 主页
 * http://127.0.0.1:80/EinkAppStore/
 *
 * @author summer502
 */
@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private HomeService homeService;

    @Autowired
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @RequestMapping(value = {"/EinkAppStore/index", "/EinkAppStore/index.html"}, produces = "text/html;charset=UTF-8")
    public String index() {
        // index.html
        return "index";
    }

    @RequestMapping(value = {"/EinkAppStore/uploadapp", "/EinkAppStore/uploadapp.html"}, produces = "text/html;charset=UTF-8")
    public String uploadapp() {
        // uploadapp.html
        return "uploadapp";
    }

    @RequestMapping("/EinkAppStore")
    public String redirectHomeHtml() {
        return "redirect:/EinkAppStore/";
    }

    @RequestMapping(value = "/EinkAppStore/**", method = RequestMethod.GET)
    public void homeHtml(HttpServletRequest request, HttpServletResponse response) {
//        System.out.println(request.getAttribute("org.apache.catalina.ASYNC_SUPPORTED"));// true
//        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        String requestURI = request.getRequestURI();

        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(3 * 60 * 1000L);
        asyncContext.addListener(new HomeHtmlAsyncTaskListener(requestURI));

        asyncTaskExecutor.execute(() -> {
            logger.info("EinkAppStore, home, pathName= {}, thread={}", requestURI, Thread.currentThread().getName());// j8-async-task-1

            homeService.homeHtml(requestURI, response);
            asyncContext.complete();
        });
    }

    @RequestMapping(value = "/EinkAppStore/upload/app", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<String> upload(AppInfoDTO appInfoDTO,
                                         @RequestParam("iconFile") MultipartFile iconFile,
                                         @RequestParam("appUrlFile") MultipartFile appUrlFile) {
        DeferredResult<String> deferredResult = new DeferredResult<>();

        asyncTaskExecutor.execute(() -> {
            logger.info("EinkAppStore, upload app, appName= {}, thread={}", appInfoDTO.getAppName(), Thread.currentThread().getName());

            boolean result = homeService.saveAppInfo(appInfoDTO, iconFile, appUrlFile);
            if (result) {
                deferredResult.setResult("redirect:/EinkAppStore/");
            } else {
                deferredResult.setResult("error");
            }
        });

        return deferredResult;
    }

}



