# FakeZhangyueEinkAppStore 伪掌阅iReader应用商店

> 根据在iReader应用商店下载app流程机制的分析，模拟搭建一个私有的应用商店服务端，给掌阅iReader安装第三方app。  
> 其实，只要能安装上一个浏览器，比如chrome、edge等，就可以再通过浏览器下载其他app安装。  
> 
> 关于iReader应用商店中下载app流程机制的分析详见：[安装app的流程分析](./docs/%E5%AE%89%E8%A3%85app%E7%9A%84%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90.md)    
>> 在安装app流程中，涉及有5个http请求：  
>> 1. app的列表分页数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0`）
>> 2. app的图标icon下载地址（`http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png`）
>> 3. app的详情数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink`）
>> 4. app的安装包下载地址（`http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip`）
>> 5. app的类别查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category`）

## 设备
| 型号   | 测试日期 |
| :----- | :--: |
| FaceNote N1s |  2023-02-20  |


## iReader接入私有的应用商店服务端
> 有2种接入方式，建议选用“配置HTTP代理服务器”。
> ![部署示意图](/docs/jietu/部署示意图.png)

### 1. 域名拦截，把“ebook.zhangyue.com”指向“服务端ip”  
劫持对“ebook.zhangyue.com”的请求，转给私有应用商店服务端处理（服务端要使用80端口），比如在路由器上添加一条hosts，如图所示：![域名拦截ebook.zhangyue.com](./docs/jietu/在局域网内拦截域名.png)

### 2. 使用HTTP代理服，配置代理服务器的主机名为“服务端ip”，端口为“80” 
在WLAN处选择对应的wifi名称，长按会弹出详情窗口，在高级选项里面配置代理，这样所有访问请求都会转给私有应用商店服务端代理去请求，服务端会把关于应用商店的请求进行拦截处理，其他请求则正向代理处理，如图所示：![在设备wlan上配置HTTP代理服务器](./docs/jietu/在设备wlan上配置HTTP代理服务器.jpg)

## 搭建服务端
### 1. 使用nginx搭建
基于`nginx`发布的http服务  
运行环境：[nginx](https://nginx.org/en/download.html)  
启动方式：[部署说明](server-nginx/README.md)  

### 2. 使用node.js搭建
基于`Node.js`编写的http服务端  
运行环境：[Node.js](https://nodejs.org/en/download/)  
启动方式：[部署说明](server-JS8zyEinkAppStore/README.md)  

### 3. 使用java搭建
用`spring boot`编写的http服务端  
运行环境：[java](https://www.java.com/zh-CN/)  
启动方式：[部署说明](server-J8zyEinkAppStore/README.md)  
