基于 nginx 搭建一个 http 服务端  

## 步骤：  
1. 下载 nginx  
2. 把"EinkAppStore"、"conf"、"html"这3个目录直接复制覆盖到 nginx 目录下  
    ![nginx 目录结构](/docs/jietu/nginx目录结构.png)
3. 启动 nginx  
4. 浏览器访问 "http://127.0.0.1:80" ，验证服务是否正常  
5. 配置 ireader 设备接入此私有应用商店服务端  

## 配置文件的说明：  
- [nginx 的配置文件](/server-nginx8zyEinkAppStore/conf/)  
    <pre><code>目录"conf"的结构
    │ nginx-geo-ip-whitelist.conf
    │ nginx.conf                                        <em><font size=1 color=green>这个是配置文件，使用命令<code>"nginx -s reload"</code>重启nginx</font></em></code></pre>

- [伪应用商店的配置文件](/server-nginx8zyEinkAppStore/EinkAppStore/)    
    <pre><code>目录"EinkAppStore"的结构
    │ AppInfo_com.coolapk.market.json
    │ AppInfo_com.jd.app.reader.json
    │ AppInfo_com.microsoft.emmx.json
    │ AppInfo_com.microsoft.office.onenote.json
    │ AppInfo_com.tencent.android.qqdownloader.json
    │ AppInfo_com.tencent.weread.eink.json
    │ AppInfo_com.zhangyue.read.iReader.eink.json       <em><font size=1 color=green>这个json文件是app详情，文件名必须以“AppInfo_***"appName"***.json”格式命名</font></em>
    │ AppList.json                                      <em><font size=1 color=green>这个json文件是app列表，里面每一个数组元素节点对应一个app详情，用来在设备应用商店页面中展示app列表的，文件名不能改动</font></em>
    │ Category.json                                     <em><font size=1 color=green>这个json文件是app类别，对应"categoryId"字段值，文件名不能改动</font></em>
    │
    └─downloads
        ├─icon
        │      CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png <em><font size=1 color=green>这个是app图标，文件名必须与json文件中"icon"配置的一样</font></em>
        │      CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png
        │      com.android.chrome.png
        │      com.coolapk.market.png
        │      com.jd.app.reader.png
        │      com.microsoft.emmx.png
        │      com.microsoft.office.onenote.png
        │      com.tencent.android.qqdownloader.png
        │
        └─zip
               com.android.chrome.zip                   <em><font size=1 color=green>这个是app安装包，必须是zip格式（需要把apk文件压缩成zip文件），文件名必须与json文件中"appUrl"配置的一样</font></em>
               com.coolapk.market.zip
               com.jd.app.reader.zip
               com.microsoft.emmx.zip
               com.tencent.android.qqdownloader.zip
               wKgHkGHFm-KEeeJdAAAAABDgnm4824219929.zip
               wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip</code></pre>

- app 详情数据节点的配置  
在目录“EinkAppStore”下的“AppList.json”和“AppInfo_\*\*\*"appName"\*\*\*.json”文件中，每个 json 对象节点表示一个 app 的详情数据，这些 json 文件控制着应用商店服务端提供下载哪些 app，对每个 app 的添加、修改、删除等操作都需要编辑改动这些 json 文件。  
    ```json
    这个json对象节点是"掌阅精选"的 app 详情数据节点
    {
      "id": 31,
      "name": "掌阅精选",
      "icon": "http://127.0.0.1:80/EinkAppStore/downloads/icon/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png",
      "appVersion": "V20.1.2",
      "appSize": "23.3MB",
      "categoryId": 2,
      "appName": "com.zhangyue.read.iReader.eink",
      "appUrl": "http://127.0.0.1:80/EinkAppStore/downloads/zip/wKgHkGHFm-KEeeJdAAAAABDgnm4824219929.zip",
      "appDesc": "",
      "explain": ""
    }
    ```  
    app 详情数据节点的主要字段：  
    1. "***id***"数字，需要全局唯一；  
    2. "***name***"展示名称；  
    3. "***icon***"展示图标，请求路径必须是"/EinkAppStore/downloads/icon/"，请求路径中的文件名需要与目录"/EinkAppStore/downloads/icon/"下的文件名对应起来，它的文件名可以用"*appName*"来命名；  
    4. "***appName***"存放安装包目录，需要全局唯一；  
    5. "***appUrl***"下载安装包，请求路径必须是"/EinkAppStore/downloads/zip/"，请求路径中的文件名需要与目录"/EinkAppStore/downloads/zip/"下的文件名对应起来，它的文件名可以用"*appName*"来命名。  

