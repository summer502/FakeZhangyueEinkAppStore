---
layout: default
title: FakeZhangyueEinkAppStore
permalink: /about
---

<div class="page-title">
{{page.title}}
</div>
<div class="content">

</div>

搭建一个伪掌阅 iReader 应用商店服务端，给设备安装第三方 app 应用。同时服务端也是个 http(s) 代理服务器，实现 http 代理和http connect 隧道代理。  

### 准备材料
1. app的名称（name）
2. app的全局唯一名称，一般使用包路径（appName）
3. app的图标（icon）
4. app的安卓系统安装包（*.apk格式）
5. app的版本（appVersion）
6. app的包大（小appSize）

### 制作 app 安装包
把安卓系统安装包apk文件压缩成zip文件

### 修改json文件配置 app 数据

### 下载服务端，更新 app 配置数据

### 启动应用商店服务端，验证服务正常

### 配置设备的网络代理为使用 HTTP 代理服务器

