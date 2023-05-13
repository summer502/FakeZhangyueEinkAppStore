---
layout: post
title: Or maybe Markdown
subtitle: This post explains how you can write posts using Markdown.
tags: [guide, markdown]
lang: en
---

This post is written in markdown, but you can also write a [post using html]({% link _posts/2020-02-27-html-posts.html %}).

<span class="color-red">[NEW]:</span> Now you can also create a private post, which will not be visible on the blog homepage, but is accessible via a URL. See [this secret post]({% link _posts/2020-04-05-private-example.html %}) as an example.

Posts should be named as `%yyyy-%mm-%dd-your-post-title-here.md`, and placed in the `_posts/` directory. Drafts can be kept in `_drafts/` directory.

-------------

# This is a heading
## This is a sub-heading
### This is a sub-sub-heading

<span class="color-blue">Some</span>
<span class="color-green">cool</span>
<span class="color-orange">colorful</span>
<span class="color-red">text.</span><br>

<span class="highlight-blue">And</span>
<span class="highlight-green">some</span>
<span class="highlight-orange">highlighting</span>
<span class="highlight-red">styles.</span>

**Here is a bulleted list,**
 - This is a bullet point
 - This is another bullet point


**Here is a numbered list,**
1. You can also number things down.
2. And so on.

**Here is a sample code snippet in C,**
```C
#include <stdio.h>

int main(void){
    printf("hello world!");
}
```

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

**Here is a horizontal rule,**

--------------

**Here is a blockquote,**

> There is no such thing as a hopeless situation. Every single 
> circumstances of your life can change!

**Here is a table,**

ID  | Name   | Subject
----|--------|--------
201 | John   | Physics
202 | Doe    | Chemistry
203 | Samson | Biology

**Here is a link,**<br>
[GitHub Inc.](https://github.com) is a web-based hosting service
for version control using Git

**Here is an image,**<br>
![](../assets/autumn.jpg)
