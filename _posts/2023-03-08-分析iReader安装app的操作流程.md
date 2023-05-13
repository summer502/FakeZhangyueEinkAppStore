---
layout: post
title: 分析iReader下载安装app的操作流程
subtitle: 通过抓包工具分析在iReader应用商店中下载安装app的操作流程，构建一个私有的应用商店服务端来安装第三方app。
tags: [EinkAppStore]
---

为了给掌阅 iReader 等设备安装第三方 app，最近通过抓包工具分析了从 iReader 应用商店中下载安装 app 的操作流程。iReader 使用 http 协议传输数据，根据其5个接口数据，构建出一个私有的应用商店服务端提供下载app，此私有服务端同时具有“http 代理服务器”的功能，会代理下载电子书等其他请求。  

设备型号：FaceNote N1s  
抓包工具：Wireshark  

抓包分析详见：[安装 app 的流程分析](https://github.com/summer502/FakeZhangyueEinkAppStore/blob/main/docs/%E5%AE%89%E8%A3%85app%E7%9A%84%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90.md)     

> 通过抓包分析后，涉及有5个 http 请求：
> 1. app 的列表分页数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0`）
> 2. app 的图标 icon 下载地址（`http://bookbk.img.ireader.com/idc_1/m_1,w_300,h_400/13b9ed15/group61/M00/92/35/CmQUOV-_Vz6EFAJgAAAAABHkPGY809880571.png`）
> 3. app 的详情数据查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink`）
> 4. app 的安装包下载地址（`http://other.d.ireader.com/group8/M00/7A/D1/wKgHkGOTLg-EPeA4AAAAALS7Yoo971970628.zip`）
> 5. app 的类别查询地址（`http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category`）

项目地址：[FakeZhangyueEinkAppStore](https://github.com/summer502/FakeZhangyueEinkAppStore)  
