# FakeZhangyueEinkAppStore 伪掌阅iReader应用商店

> 根据在iReader应用商店下载app流程机制的分析，模拟搭建一个私有的应用商店服务端，给掌阅iReader安装第三方app。
> 其实，只要能安装上一个浏览器，比如chrome、edge等，就可以再通过浏览器下载其他app安装。

关于iReader应用商店中下载app流程机制的分析详见：[安装app的流程分析](./docs/%E5%AE%89%E8%A3%85app%E7%9A%84%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90.md)
> 在安装app流程中，涉及有5个http请求：
> 1. app的列表分页数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0`）
> 2. app的图标icon下载地址（`http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png`）
> 3. app的详情数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink`）
> 4. app的安装包下载地址（`http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip`）
> 5. app的类别查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category`）

| 设备   | 测试日期 |
| :----- | :--: |
| FaceNote N1s |  2023-02-20  |

## 1. 使用nginx搭建
## 2. 使用node.js搭建
## 3. 使用java搭建
