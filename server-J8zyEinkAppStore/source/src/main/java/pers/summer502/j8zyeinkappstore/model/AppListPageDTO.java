package pers.summer502.j8zyeinkappstore.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppListPageDTO {

    private final List<Map<String, Object>> list = new ArrayList<>();

    private final Map<String, Integer> page = new LinkedHashMap<>();

    public void addList(int id, String name, String icon, String appVersion, String appSize, String appName, String appDesc) {
        Map<String, Object> appInfo = new LinkedHashMap<>();
        appInfo.put("id", id);
        appInfo.put("name", name);
        appInfo.put("icon", icon);
        appInfo.put("appVersion", appVersion);
        appInfo.put("appSize", appSize);
        appInfo.put("appName", appName);
        appInfo.put("appDesc", appDesc);
        list.add(appInfo);
    }

    public void addPage(int currentPage, int pageSize, int totalPage, int totalRecord) {
        page.put("currentPage", currentPage);
        page.put("pageSize", pageSize);
        page.put("totalPage", totalPage);
        page.put("totalRecord", totalRecord);
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

    public Map<String, Integer> getPage() {
        return page;
    }
}
