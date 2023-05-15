---
layout: post
title: Sample draft
subtitle: This is a draft - it won't be visible on the blog
tags: [EinkAppStore]
---

Once the draft is complete, it should be moved to `_posts/` directory to see it on the blog


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



