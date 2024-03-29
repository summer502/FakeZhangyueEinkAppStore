基于 netty 搭建一个 http 服务端  

## 要求：  
- Java 17
- Spring Boot 3
- Netty 4.1
- Maven (>=3.3.3)
[tomcat对http connect请求的处理](https://summer502.github.io/FakeZhangyueEinkAppStore/2023-05-13-tomcat%E5%AF%B9http-connect%E7%9A%84%E5%A4%84%E7%90%86/)
## 步骤：  
1. 下载和安装 jdk17  
2. 进入"/FakeZhangyueEinkAppStore/server-J8zyEinkAppStore"这个目录，构建项目  
3. 构建项目  
    1. 构建环境要求：  
        - Java 17
        - Maven (>=3.3.3)   

    2. 构建命令：  
        ```console
        $ # 进入代码目录
        $ cd /FakeZhangyueEinkAppStore/server-J8zyEinkAppStore/source
        $ # 编译
        $ ./mvnw install
        ```
4. 启动项目  
    ```console
    $ cd /FakeZhangyueEinkAppStore/server-J8zyEinkAppStore
    $ java -jar ./J8zyEinkAppStore.jar
    ```
5. 浏览器访问 "http://127.0.0.1:80" ，验证服务是否正常  
6. 配置 ireader 设备接入此私有应用商店服务端  

## 配置文件的说明：  
- [伪应用商店的配置文件](/server-JS8zyEinkAppStore/EinkAppStore/)  
    <pre><code>目录"EinkAppStore"的结构
    │ AppList_AppInfo.json                              <em style="color:green">这个json文件是app列表，里面每一个数组元素节点对应一个app详情，用来在设备应用商店页面中展示app列表和详情的，文件名不能改动</em>
    │ Category.json                                     <em style="color:green">这个json文件是app类别，对应"categoryId"字段值，文件名不能改动</em>
    │
    └─downloads
        ├─icon
        │      CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png <em style="color:green">这个是app图标，文件名必须与json文件中"icon"配置的一样</em>
        │      CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png
        │      com.android.chrome.png
        │      com.jd.app.reader.png
        │
        └─zip
               com.android.chrome.zip                   <em style="color:green">这个是app安装包，必须是zip格式（需要把apk文件压缩成zip文件），文件名必须与json文件中"appUrl"配置的一样</em>
               com.jd.app.reader.zip
               wKgHkGHFm-KEeeJdAAAAABDgnm4824219929.zip
               wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip</code></pre>

- app 详情数据节点的配置  
在目录“EinkAppStore”下的“AppList_AppInfo.json”文件中，每个 json 对象节点表示一个 app 的详情数据，这个 json 文件控制着应用商店服务端提供下载哪些 app，对每个 app 的添加、修改、删除等操作都需要编辑改动此 json 文件。  
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

