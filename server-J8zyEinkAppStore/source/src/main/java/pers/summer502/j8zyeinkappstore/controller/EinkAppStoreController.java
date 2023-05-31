package pers.summer502.j8zyeinkappstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;
import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;
import pers.summer502.j8zyeinkappstore.model.AppListPageDTO;
import pers.summer502.j8zyeinkappstore.model.CategoryDTO;
import pers.summer502.j8zyeinkappstore.service.EinkAppStoreService;
import pers.summer502.j8zyeinkappstore.util.ResponseBodyResult;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 伪应用商店
 * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0
 * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink
 * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category
 *
 * @author summer502
 */
@RestController
@RequestMapping({"/fakeAppStore/zybook3", "/zybook*"})
public class EinkAppStoreController {
    private final Logger logger = LoggerFactory.getLogger(EinkAppStoreController.class);

    @Autowired
    private EinkAppStoreService appStoreService;

    @Autowired
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    /**
     * 在 RequestMapping#params 未匹配上时，会走这个
     */
    @GetMapping(value = "/app/app.php")
    public void other(@RequestParam(value = "ca", required = false) String ca) {
        // 非 ca=Eink_AppStore.AppList、ca=Eink_AppStore.AppInfo、ca=Eink_AppStore.Category 的请求都要走 HTTP 代理，见拦截器的preHandle方法
        logger.info("other, ca={}, thread={}", ca, Thread.currentThread().getName());
    }

    /**
     * app列表分页数据查询地址“ca=Eink_AppStore.AppList”
     * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0
     *
     * @param ca         Eink_AppStore.AppList
     * @param page       1
     * @param size       7
     * @param categoryId 0
     */
    @GetMapping(value = "/app/app.php", params = {"ca=Eink_AppStore.AppList", "page", "size"})
    public Callable<ResponseBodyResult<AppListPageDTO>> getAppList(@RequestParam(value = "ca", required = false) String ca,
                                                                   @RequestParam(value = "page", required = false) Integer page,
                                                                   @RequestParam(value = "size", required = false) Integer size,
                                                                   @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        Callable<ResponseBodyResult<AppListPageDTO>> task = new Callable<>() {
            @Override
            public ResponseBodyResult<AppListPageDTO> call() throws Exception {
                logger.info("getAppList, ca={}, thread={}", ca, Thread.currentThread().getName());// MvcAsync2
                if (page == null || size == null || categoryId == null || page <= 0 || size <= 0 || categoryId < 0) {
                    return ResponseBodyResult.error("参数page、size、categoryId值只能是数字");
                }

                AppListPageDTO appList = appStoreService.getAppList(page, size, categoryId);
                if (appList == null) {
                    return ResponseBodyResult.error("读EinkAppStore/AppList_AppInfo.json失败");
                } else {
                    return ResponseBodyResult.success(appList);
                }
            }
        };

        return task;
    }

    /**
     * app详情数据查询地址“ca=Eink_AppStore.AppInfo”
     * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink
     *
     * @param ca      Eink_AppStore.AppInfo
     * @param appName com.zhangyue.read.iReader.eink
     */
    @GetMapping(value = "/app/app.php", params = {"ca=Eink_AppStore.AppInfo", "appName"})
    public WebAsyncTask<ResponseBodyResult<AppInfoDTO>> getAppInfo(@RequestParam(value = "ca", required = false) String ca,
                                                                   @RequestParam(value = "appName", required = false) String appName) {
        Callable<ResponseBodyResult<AppInfoDTO>> task = () -> {
            logger.info("getAppInfo, ca={}, thread={}", ca, Thread.currentThread().getName());// j8-async-task-1
            if (appName == null || appName.isBlank()) {
                return ResponseBodyResult.error("参数appName值为空");
            }

            AppInfoDTO appInfo = appStoreService.getAppInfo(appName);
            if (appInfo == null) {
                return ResponseBodyResult.error("读EinkAppStore/AppList_AppInfo.json失败");
            } else {
                return ResponseBodyResult.success(appInfo);
            }
        };

        WebAsyncTask<ResponseBodyResult<AppInfoDTO>> webAsyncTask = new WebAsyncTask<>(3 * 1000L, asyncTaskExecutor, task);
        webAsyncTask.onTimeout(() -> {
            return ResponseBodyResult.error("超时");
        });
        webAsyncTask.onCompletion(() -> {
            return;
        });
        return webAsyncTask;
    }

    /**
     * app类别查询地址“ca=Eink_AppStore.Category”
     * http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category
     *
     * @param ca Eink_AppStore.Category
     */
    @GetMapping(value = "/app/app.php", params = "ca=Eink_AppStore.Category")
    public WebAsyncTask<ResponseBodyResult<List<CategoryDTO>>> getCategory(@RequestParam(value = "ca", required = false) String ca) {
        Callable<ResponseBodyResult<List<CategoryDTO>>> task = () -> {
            logger.info("getCategory, ca={}, thread={}", ca, Thread.currentThread().getName());// j8-async-task-1
            if (ca == null) {
                return ResponseBodyResult.error("值为空");
            }

            List<CategoryDTO> categoryS = appStoreService.getCategory();
            if (categoryS == null) {
                return ResponseBodyResult.error("读EinkAppStore/Category.json失败");
            } else {
                return ResponseBodyResult.success(categoryS);
            }
        };

        WebAsyncTask<ResponseBodyResult<List<CategoryDTO>>> webAsyncTask = new WebAsyncTask<>(3 * 1000L, asyncTaskExecutor, task);
        webAsyncTask.onError(() -> {
            logger.error("getCategory, onError, thread={}", Thread.currentThread().getName());
            return ResponseBodyResult.error("出错");
        });
        webAsyncTask.onTimeout(() -> {
            logger.error("getCategory, onTimeout, thread={}", Thread.currentThread().getName());// http-nio-80-exec-2
            return ResponseBodyResult.error("超时");
        });
        return webAsyncTask;
    }


}



