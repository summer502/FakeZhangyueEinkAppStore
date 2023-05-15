---
layout: post
title: FakeZhangyueEinkAppStore
subtitle: 搭建一个伪掌阅 iReader 应用商店服务端，给设备安装第三方 app 应用。
tags: [EinkAppStore]
---
# FakeZhangyueEinkAppStore
## 准备
### 制作 app 安装包
### 修改json文件配置 app 数据
### 下载服务端，更新 app 配置数据
### 启动应用商店服务端，验证服务正常
### 配置设备的网络代理为使用 HTTP 代理服务器



## 高亮代码片段
Jekyll 自带语法高亮功能，你可以选择使用 Pygments 或 Rouge 两种工具中的一种。在文章中插入一段高亮代码非常容易，只需使用下面的 Liquid 标记：
{% highlight ruby %}
def show
  @widget = Widget(params[:id])
  respond_to do |format|
    format.html # show.html.erb
    format.json { render json: @widget }
  end
end
{% endhighlight %}



