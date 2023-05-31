package pers.summer502.j8zyeinkappstore.service;

import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;
import pers.summer502.j8zyeinkappstore.model.AppListPageDTO;
import pers.summer502.j8zyeinkappstore.model.CategoryDTO;

import java.util.List;

public interface EinkAppStoreService {
    /**
     * app列表分页数据查询地址“ca=Eink_AppStore.AppList”
     * ca=Eink_AppStore.AppList
     *
     * @param page       1
     * @param size       7
     * @param categoryId 0
     */
    AppListPageDTO getAppList(int page, int size, int categoryId);

    /**
     * app详情数据查询地址“ca=Eink_AppStore.AppInfo”
     * ca=Eink_AppStore.AppInfo
     *
     * @param appName com.zhangyue.read.iReader.eink
     */
    AppInfoDTO getAppInfo(String appName);

    /**
     * app类别查询地址“ca=Eink_AppStore.Category”
     * ca=Eink_AppStore.Category
     */
    List<CategoryDTO> getCategory();


}
