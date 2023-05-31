package pers.summer502.j8zyeinkappstore.service.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;
import pers.summer502.j8zyeinkappstore.service.HomeService;
import pers.summer502.j8zyeinkappstore.util.JsonUtils;
import pers.summer502.j8zyeinkappstore.util.StringUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@Service
public class HomeServiceImpl implements HomeService {
    private final Logger logger = LoggerFactory.getLogger(HomeServiceImpl.class);

    private final String storeDir = "EinkAppStore/";

    private final String homePath = new ApplicationHome(getClass()).getDir().getPath();

    private void download(File pathNameFile, HttpServletResponse response) throws IOException {
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        try {
            fileInputStream = new FileInputStream(pathNameFile);
            outputStream = response.getOutputStream();
            int available = fileInputStream.available();
            logger.info("download文件大小{}", available);

            byte[] buffer = new byte[100 * 1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFileIndexHtml(File pathNameFile) {
        // 生成"Index of /view-file/"页面
        StringBuilder str = new StringBuilder();
        str.append("<!DOCTYPE html>");
        str.append("<html>");
        str.append("<head><title>Index of /view-file/</title></head>");
        str.append("<body>");
        str.append("<h1>Index of /view-file/</h1>");
        str.append("<hr>");
        str.append("<pre>\r\n");
        if ("EinkAppStore".equals(pathNameFile.getName())) {
            str.append("<a href='/EinkAppStore/'>/</a>\r\n");
        } else {
            str.append("<a href='../'>../</a>\r\n");
        }

        File[] dirList = pathNameFile.listFiles(File::isDirectory);
        File[] fileList = pathNameFile.listFiles(File::isFile);

        // 文件名的长度的最大值
        int maxfileNameLen = Stream.of(Optional.ofNullable(dirList).orElse(new File[]{}),
                        Optional.ofNullable(fileList).orElse(new File[]{}))
                .flatMap(Arrays::stream)
                .mapToInt(file -> StringUtils.byteLength(file.getName()))
                .max()
                .orElse(0);

        // 目录
        Stream.of(Optional.ofNullable(dirList).orElse(new File[]{}))
                .sorted()
                .forEach(dir -> {
                    String name = dir.getName();
                    long mtime = dir.lastModified();
                    int length = StringUtils.byteLength(name);
                    String aUrl = name + "/";
                    str.append("<a href='" + aUrl + "'>" + name + "/</a>      " + this.fillBlank(length, maxfileNameLen) + new Date(mtime).toString() + "       " + "-\r\n");
                });

        // 文件
        Stream.of(Optional.ofNullable(fileList).orElse(new File[]{}))
                .sorted()
                .forEach(file -> {
                    String name = file.getName();
                    long mtime = file.lastModified();
                    long size = file.length();
                    int length = StringUtils.byteLength(name);
                    String aUrl = name;
                    str.append("<a href='" + aUrl + "'>" + name + "</a>       " + this.fillBlank(length, maxfileNameLen) + new Date(mtime).toString() + "       " + size + "\r\n");
                });

        str.append("</pre>");
        str.append("<hr>");
        str.append("</body>");
        str.append("</html>");
        return str.toString();
    }

    // 填充空格
    private String fillBlank(int fileNameLength, int maxfileNameLen) {
        StringBuilder fillBlank = new StringBuilder();
        if (fileNameLength < maxfileNameLen) {
            for (int i = 0; i < maxfileNameLen - fileNameLength; i++) {
                fillBlank.append(' ');
            }
        }
        return fillBlank.toString();
    }

    @Override
    public void homeHtml(String pathName, HttpServletResponse response) {
        if (!pathName.startsWith("/EinkAppStore/")) {
            logger.error("目录不正确，必须是 EinkAppStore");
            return;
        }

        String path = homePath + pathName;
        File pathNameFile = new File(path);

        try {
            if (pathNameFile.exists()) {
                if (pathNameFile.isFile()) {
                    String name = pathNameFile.getName();
                    // 下载
                    response.setStatus(200);
                    response.setHeader("Content-Type", "application/octet-stream");
                    response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                    response.setHeader("Last-Modified", new Date().toString());
                    response.setHeader("ETag", String.valueOf(System.currentTimeMillis()));
                    download(pathNameFile, response);
                } else if (pathNameFile.isDirectory()) {
                    // 生成"Index of /view-file/"页面
                    String fileIndexHtml = getFileIndexHtml(pathNameFile);
                    // 响应
                    response.setStatus(200);
                    response.setHeader("Content-Type", "text/html; charset=utf8");
                    PrintWriter writer = response.getWriter();
                    writer.println(fileIndexHtml);
                    writer.flush();
                } else {
                    logger.error("未知的文件类型fileName: {}", pathName);
                }
            } else {
                logger.error(pathName + "文件不存在。");
                response.setStatus(404);
                response.setHeader("Content-Type", "text/html");
                PrintWriter writer = response.getWriter();
                writer.println(pathName + "文件不存在。");
                writer.flush();
            }
        } catch (IOException e) {
            logger.error("读取" + pathName + "文件失败。" + e.getMessage());
            response.setStatus(500);
        }
    }

    @Override
    public boolean saveAppInfo(AppInfoDTO appInfoDTO, MultipartFile iconFile, MultipartFile appUrlFile) {
        boolean result = false;
        String appName = appInfoDTO.getAppName();
        try {
            if (!iconFile.isEmpty()) {
                String originalFilename = iconFile.getOriginalFilename();
                String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = appName + suffixName;

                //本地文件夹
                String path = homePath + "/EinkAppStore/downloads/icon/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, fileName);
                iconFile.transferTo(file);
                logger.info("iconFile={}{}", path, fileName);
                String iconStr = "http://127.0.0.1:80/EinkAppStore/downloads/icon/" + fileName;
                appInfoDTO.setIcon(iconStr);
            }
            if (!appUrlFile.isEmpty()) {
                String originalFilename = appUrlFile.getOriginalFilename();
                String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = appName + suffixName;

                //本地文件夹
                String path = homePath + "/EinkAppStore/downloads/zip/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, fileName);
                appUrlFile.transferTo(file);
                logger.info("saveAppInfo, appUrlFile={}{}", path, fileName);
                String appUrlStr = "http://127.0.0.1:80/EinkAppStore/downloads/zip/" + fileName;
                appInfoDTO.setAppUrl(appUrlStr);
            }

            result = true;
        } catch (IOException e) {
            result = false;
            logger.error("saveAppInfo, error={}", e.getMessage());
        }

        if (!result) {
            return false;
        }

        try {
            // 读取 AppList_AppInfo.json
            String jsonFileName = "/EinkAppStore/AppList_AppInfo.json";
            File jsonFile = new File(homePath + jsonFileName);
            if (!jsonFile.exists()) {
                logger.error("saveAppInfo, 文件{}不存在", jsonFileName);
                return false;
            }

            boolean isAdd = true;
            List<Object> list = JsonUtils.toList(jsonFile);
            for (Object appInfo : list) {
                Map<String, Object> map = (Map<String, Object>) appInfo;
                String appNameD = (String) map.get("appName");
                if (appName.equalsIgnoreCase(appNameD)) {
                    // 替换
                    map.put("id", appInfoDTO.getId());
                    map.put("name", appInfoDTO.getName());
                    map.put("icon", appInfoDTO.getIcon());
                    map.put("appVersion", appInfoDTO.getAppVersion());
                    map.put("appSize", appInfoDTO.getAppSize());
                    map.put("categoryId", appInfoDTO.getCategoryId());
                    map.put("appName", appName);
                    map.put("appUrl", appInfoDTO.getAppUrl());
                    map.put("appDesc", appInfoDTO.getAppDesc());
                    map.put("explain", appInfoDTO.getExplain());
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                // 新增
                String jsonStr = JsonUtils.toStr(appInfoDTO);
                Map<String, Object> map = JsonUtils.toMap(jsonStr);
                list.add(map);
            }

            // 写入 AppList_AppInfo.json
            String content = JsonUtils.toPrettyStr(list);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jsonFile))) {
                bufferedWriter.write(content);
            }

            result = true;
        } catch (IOException e) {
            result = false;
            logger.error("saveAppInfo, error={}", e.getMessage());
        }

        return result;
    }
}
