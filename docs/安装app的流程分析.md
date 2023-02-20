为了给掌阅iReader安装第三方app，通过抓包工具分析在iReader应用商店中安装app的操作流程，了解其应用商店下载app的运行机制，进而构建一个私有的应用商店服务端。  

设备型号：FaceNote N1s  
抓包工具：Wireshark  

> 通过抓包分析后，涉及有5个http请求：
> 1. app的列表分页数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0`）
> 2. app的图标icon下载地址（`http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png`）
> 3. app的详情数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink`）
> 4. app的安装包下载地址（`http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip`）
> 5. app的类别查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category`）

## N1s的应用商店  
- [应用商店入口位置](./jietu/N1s_AppStore_1.jpg)  
- [应用商店中的应用列表](./jietu/N1s_AppStore_2.jpg)  

## 安装app的操作流程
### 第一步，点击应用商店图标，进入应用商店页面
1. 这时会发起一个http请求，分页数据查询，查询AppList数据  
请求：GET  
`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0&zysid=&usr=&rgt=7&p1=&pc=100&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink&zysid=&zysign=`  
响应：Content-Type: application/json; charset=utf-8  
`{"code":0,"msg":"","body":{"list":[{"id":31,"name":"\u638c\u9605\u7cbe\u9009","icon":"http:\/\/bookbk.img.ireader.com\/idc_1\/m_1,w_300,h_400\/13b9ed15\/group61\/M00\/92\/35\/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png?v=bdhhb10L&t=CmQUOV-_Vz4.","appVersion":"V20.1.2","appSize":"23.3MB","appName":"com.zhangyue.read.iReader.eink","appDesc":""},{"id":2,"name":"\u5fae\u4fe1\u8bfb\u4e66","icon":"http:\/\/bookbk.img.ireader.com\/idc_1\/m_1,w_300,h_400\/75013979\/group61\/M00\/EE\/E0\/CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png?v=ckr1QuK3&t=CmQUOGEwpDo.","appVersion":"V1.9.1","appSize":"31.2MB","appName":"com.tencent.weread.eink","appDesc":""}],"page":{"currentPage":1,"pageSize":7,"totalPage":1,"totalRecord":2}}}`  
[http请求ca=Eink_AppStore.AppList的抓包截图](./jietu/http%E8%AF%B7%E6%B1%82ca%3DEink_AppStore.AppList%E7%9A%84%E6%8A%93%E5%8C%85.png)  

    [http请求ca=Eink_AppStore.AppList的浏览器截图](./jietu/http%E8%AF%B7%E6%B1%82ca%3DEink_AppStore.AppList%E7%9A%84%E6%B5%8F%E8%A7%88%E5%99%A8.png)  
    ```json
    {
      "code": 0,
      "msg": "",
      "body": {
        "list": [
          {
            "id": 31,
            "name": "掌阅精选",
            "icon": "http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png?v=bdhhb10L&t=CmQUOV-_Vz4.",
            "appVersion": "V20.1.2",
            "appSize": "23.3MB",
            "appName": "com.zhangyue.read.iReader.eink",
            "appDesc": ""
          },
          {
            "id": 2,
            "name": "微信读书",
            "icon": "http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/75013979/group61/M00/EE/E0/CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png?v=ckr1QuK3&t=CmQUOGEwpDo.",
            "appVersion": "V1.9.1",
            "appSize": "31.2MB",
            "appName": "com.tencent.weread.eink",
            "appDesc": ""
          }
        ],
        "page": {
          "currentPage": 1,
          "pageSize": 7,
          "totalPage": 1,
          "totalRecord": 2
        }
      }
    }
    ```

2. 接着又发起一个http请求，获取app的图标"icon"  
请求：GET  
`http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png?v=bdhhb10L&t=CmQUOV-_Vz4.`    

    掌阅应用商店的2个app图标见【[/server-nginx/nginx/EinkAppStore/downloads/icon/](/server-nginx/nginx/EinkAppStore/downloads/icon/)】中的：`CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png` 掌阅精选、`CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png` 微信读书。  

### 第二步，点击下载按钮，进行安装应用
1. 这时会发起一个http请求，详情数据查询，查询AppInfo数据  
请求：GET  
`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.tencent.weread.eink&zysid=&usr=&rgt=&p1=&pc=&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink&zysid=&zysign=`  
响应：Content-Type: application/json; charset=utf-8  
 `{"code":0,"msg":"","body":{"id":2,"name":"\u5fae\u4fe1\u8bfb\u4e66","icon":"http:\/\/bookbk.img.ireader.com\/idc_1\/m_1,w_300,h_400\/75013979\/group61\/M00\/EE\/E0\/CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png?v=ckr1QuK3&t=CmQUOGEwpDo.","appVersion":"V1.9.1","appSize":"31.2MB","categoryId":2,"appName":"com.tencent.weread.eink","appUrl":"http:\/\/other.d.ireader.com\/group8\/M00\/7A\/D1\/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip?v=wPVHDkX6&t=wKgHkGOTLg8.","appDesc":"","explain":""}}`  
 
    [http请求ca=Eink_AppStore.AppInfo的浏览器截图](./jietu/http%E8%AF%B7%E6%B1%82ca%3DEink_AppStore.AppInfo%E7%9A%84%E6%B5%8F%E8%A7%88%E5%99%A8.png)  
    ```json
    {
      "code": 0,
      "msg": "",
      "body": {
        "id": 2,
        "name": "微信读书",
        "icon": "http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/75013979/group61/M00/EE/E0/CmQUOGEwpDqEX51AAAAAAAeM1VA414608802.png?v=ckr1QuK3&t=CmQUOGEwpDo.",
        "appVersion": "V1.9.1",
        "appSize": "31.2MB",
        "categoryId": 2,
        "appName": "com.tencent.weread.eink",
        "appUrl": "http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip?v=wPVHDkX6&t=wKgHkGOTLg8.",
        "appDesc": "",
        "explain": ""
      }
    }
    ```

2. 然后请求下载地址"appUrl"，下载安装包“.zip”文件  
请求：GET  
`http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip?v=wPVHDkX6&t=wKgHkGOTLg8.`  
响应：Content-Type: application/zip  
[http请求app安装包下载地址的抓包截图](./jietu/http%E8%AF%B7%E6%B1%82app%E5%AE%89%E8%A3%85%E5%8C%85%E4%B8%8B%E8%BD%BD%E5%9C%B0%E5%9D%80%E7%9A%84%E6%8A%93%E5%8C%85.png)  

    掌阅应用商店的2个app安装包见【[/server-nginx/nginx/EinkAppStore/downloads/zip/](/server-nginx/nginx/EinkAppStore/downloads/zip/)】中的：`wKgHkGHFm-KEeeJdAAAAABDgnm4824219929.zip` 掌阅精选、`wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip` 微信读书。  
    
    [http请求下载.zip文件解压后的目录结构截图](./jietu/http%E8%AF%B7%E6%B1%82%E4%B8%8B%E8%BD%BD.zip%E6%96%87%E4%BB%B6%E8%A7%A3%E5%8E%8B%E5%90%8E%E7%9A%84%E7%9B%AE%E5%BD%95%E7%BB%93%E6%9E%84.png)  
    > 由“.zip”文件的目录结构可以看出，这是把apk文件直接压缩成zip文件。  

3. 下载“.zip”文件完成后，又发起一次请求，查询app的类别Category  
请求：GET  
`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category&zysid=&usr=&rgt=&p1=&pc=&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink&zysid=&zysign=`  
响应：Content-Type: application/json; charset=utf-8  
`{"code":0,"msg":"","body":[{"id":0,"label":"\u5168\u90e8"},{"id":1,"label":"\u6d4b\u8bd5"},{"id":2,"label":"\u9605\u8bfb"}]}`  

    [http请求ca=Eink_AppStore.Category的浏览器截图](./jietu/http%E8%AF%B7%E6%B1%82ca%3DEink_AppStore.Category%E7%9A%84%E6%B5%8F%E8%A7%88%E5%99%A8.png)
    ```json
    {
      "code": 0,
      "msg": "",
      "body": [
        {
          "id": 0,
          "label": "全部"
        },
        {
          "id": 1,
          "label": "测试"
        },
        {
          "id": 2,
          "label": "阅读"
        }
      ]
    }
    ```

## 一些其他的请求：  
1. `http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_Vip.Index&zysid=&usr=&rgt=7&p1=&pc=&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink&zysid=&zysign=`  
2. `http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_Shelf.BookUpdate&bookIds=12619203,11792737,11532597,11671501,11222331&tingBookIds=&albumIds=&zysid=&usr=&rgt=7&p1=&pc=&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink&zysid=&zysign=`  
3. `http://ebook.zhangyue.com/zybook/u/p/api.php?Act=getSource&type=1&zysid=&usr=&rgt=&p1=&pc=&p2=&p3=&p4=&p5=&p7=&p16=FaceNote+N1s&p33=com.zhangyue.iReader.Eink`  
