package pers.summer502.j8zyeinkappstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;
import pers.summer502.j8zyeinkappstore.model.AppListPageDTO;
import pers.summer502.j8zyeinkappstore.model.CategoryDTO;
import pers.summer502.j8zyeinkappstore.service.EinkAppStoreService;
import pers.summer502.j8zyeinkappstore.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EinkAppStoreServiceImpl implements EinkAppStoreService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebApplicationContext wac;

    private final String storeDir = "EinkAppStore/";

    private final String homePath = new ApplicationHome(getClass()).getDir().getPath();

    @Override
    public AppListPageDTO getAppList(int currentPage, int pageSize, int categoryId) {
        String jsonFileName = "AppList_AppInfo.json";
        String path = homePath + "/" + storeDir + jsonFileName;
        path = storeDir + jsonFileName;

        List<Object> list = null;
        try {
            // 读取 AppList_AppInfo.json
            File file = ResourceUtils.getFile(path);
            list = JsonUtils.toList(file);
        } catch (IOException e) {
            logger.error("getAppList, error={}", e.getMessage());
        }
        if (list == null) {
            return null;
        }

        int offset = (currentPage - 1) * pageSize;
        int limit = currentPage * pageSize;
        List<Object> appInfos = getList(offset, limit, categoryId, list);

        int totalRecord = total(categoryId, list);
        int totalPage = (int) Math.ceil((double) totalRecord / pageSize);

        AppListPageDTO appListPage = new AppListPageDTO();
        appListPage.addPage(currentPage, pageSize, totalPage, totalRecord);
        for (Object appInfo : appInfos) {
            Map<String, Object> map = (Map<String, Object>) appInfo;
            int id = (int) map.get("id");
            String name = (String) map.get("name");
            String icon = (String) map.get("icon");
            String appVersion = (String) map.get("appVersion");
            String appSize = (String) map.get("appSize");
            String appName = (String) map.get("appName");
            String appDesc = (String) map.get("appDesc");
            appListPage.addList(id, name, icon, appVersion,
                    appSize, appName, appDesc);
        }

        return appListPage;
    }

    private List<Object> getList(int offset, int limit, int categoryId, List<Object> list) {
        if (offset >= list.size()) {
            return new ArrayList<>(0);
        }
        if ((limit + offset) >= list.size()) {
            return list.subList(offset, list.size() - 1);
        }
        return list.subList(offset, limit + offset);
    }

    private int total(int categoryId, List<Object> list) {
        return list.size();
    }

    private AppInfoDTO getObject(String appName, List<Object> list) {
        AppInfoDTO appInfoDTO = new AppInfoDTO();

        for (Object appInfo : list) {
            Map<String, Object> map = (Map<String, Object>) appInfo;
            String appNameD = (String) map.get("appName");
            if (appName.equalsIgnoreCase(appNameD)) {
                int id = (int) map.get("id");
                String name = (String) map.get("name");
                String icon = (String) map.get("icon");
                String appVersion = (String) map.get("appVersion");
                String appSize = (String) map.get("appSize");
                int categoryId = (int) map.get("categoryId");
                String appUrl = (String) map.get("appUrl");
                String appDesc = (String) map.get("appDesc");
                String explain = (String) map.get("explain");

                appInfoDTO.setId(id);
                appInfoDTO.setName(name);
                appInfoDTO.setIcon(icon);
                appInfoDTO.setAppVersion(appVersion);
                appInfoDTO.setAppSize(appSize);
                appInfoDTO.setCategoryId(categoryId);
                appInfoDTO.setAppName(appNameD);
                appInfoDTO.setAppUrl(appUrl);
                appInfoDTO.setAppDesc(appDesc);
                appInfoDTO.setExplain(explain);
                return appInfoDTO;
            }
        }
        return appInfoDTO;
    }

    @Override
    public AppInfoDTO getAppInfo(String appName) {
        String jsonFileName = "AppList_AppInfo.json";
        String path = homePath + "/" + storeDir + jsonFileName;
        path = storeDir + jsonFileName;

        List<Object> list = null;
        try {
            // 读取 AppList_AppInfo.json
            File file = ResourceUtils.getFile(path);
            list = JsonUtils.toList(file);
        } catch (IOException e) {
            logger.error("getAppInfo, error={}", e.getMessage());
        }
        if (list == null) {
            return null;
        }

        AppInfoDTO appInfoDTO = getObject(appName, list);
        return appInfoDTO;
    }

    @Override
    public List<CategoryDTO> getCategory() {
        String jsonFileName = "Category.json";
        String path = homePath + "/" + storeDir + jsonFileName;
        path = storeDir + jsonFileName;

        List<Object> list = null;
        try {
            // 读取 Category.json
            File file = ResourceUtils.getFile(path);
            list = JsonUtils.toList(file);
        } catch (IOException e) {
            logger.error("getCategory, error={}", e.getMessage());
        }
        if (list == null) {
            return null;
        }
        ArrayList<CategoryDTO> objects = new ArrayList<>();
        for (Object category : list) {
            Map<String, Object> map = (Map<String, Object>) category;
            Integer id = (Integer) map.get("id");
            String label = (String) map.get("label");
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(id);
            categoryDTO.setLabel(label);
            objects.add(categoryDTO);
        }

        return objects;
    }


}
