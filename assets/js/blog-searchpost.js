
function getSearchEngine() {
  let myblog;

  function init(myBlog) {
    // 搜索输入框
    let searchInputElement = document.getElementById('search-input');
    if (!searchInputElement) {
      return;
    } else {
      myblog = myBlog;
      myblog.searchInputElement = searchInputElement;
    }

    addEventListener();
  }

  function addEventListener() {
    if (myblog.searchInputElement) {
      myblog.addEventListener(myblog.searchInputElement, 'input', function (event) {
        search(event.target.value)
      });

      myblog.addEventListener(myblog.searchInputElement, 'compositionstart', function (event) {

      });

      myblog.addEventListener(myblog.searchInputElement, 'compositionend', function (event) {
        search(event.target.value)
      });
    }
  }

  // trimKeyword
  function trimKeyword(keyword) {
    keyword = keyword.replace(/^\s+|\s+$/g, '');
    // < > & 替换
    keyword = keyword.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/&/g, '&amp;');
    return keyword;
  }

  // encodeRegChar
  function encodeRegChar(str) {
    // \ 必须在第一位
    let arr = ['\\', '.', '^', '$', '*', '+', '?', '{', '}', '[', ']', '|', '(', ')'];
    arr.forEach(function (c) {
      let r = new RegExp('\\' + c, 'g');
      str = str.replace(r, '\\' + c);
    });
    return str;
  }

  // searchExecution
  function searchExecution(keyword, posts) {
    if (!keyword || !posts) {
      return null;
    }
    keyword = trimKeyword(keyword);
    if (keyword == '') {
      return null;
    }
    let search_result_posts = [];
    let showStrlength = 100;

    // 当使用 RegExp 构造函数创造正则对象时，需要常规的字符转义规则（在前面加反斜杠 \）。比如，以下是等价的：
    // var re = new RegExp("\\w+");
    // var re = /\w+/;
    let keyword_patt_i = new RegExp(encodeRegChar(keyword), 'i');
    let keyword_patt_g_i = new RegExp(encodeRegChar(keyword), 'gi');

    for (let i = 0; i < posts.length; i++) {
      let title = posts[i].title;
      let content = posts[i].content;
      let date = posts[i].date;
      let url = posts[i].url;

      let hit = false;
      let showTitle;
      let showContent;

      if (title) {
        // 检测 title 是否匹配正则模式
        if (keyword_patt_g_i.test(title)) {
          // title 全局替换
          showTitle = title.replace(keyword_patt_g_i, '<span class="hint">' + keyword + '</span>');
          hit = true;
        } else {
          showTitle = title;
        }
      }

      if (content) {
        // 检索 content 中的 keyword
        let result = keyword_patt_i.exec(content);
        if (result) {
          // 检索时找到第一个 keyword 的位置
          let index = result.index;

          let begin = (index - 10) < 0 ? 0 : index - 10;
          let end = begin + showStrlength;
          showContent = content.substring(begin, end);
          // content 全局替换
          showContent = showContent.replace(keyword_patt_g_i, '<span class="hint">' + keyword + '</span>');
          showContent = showContent + '...';
          hit = true;
        } else {
          //  content 未命中时
          if (hit) {
            // 标题命中，则 content 直接展示
            let begin = 0;
            let end = begin + showStrlength;
            showContent = content.substring(begin, end);
            showContent = showContent + '...';
          }
        }
      }

      if (hit) {
        let post = {
          "title": showTitle,
          "content": showContent,
          "date": date,
          "url": url
        };
        search_result_posts.push(post);
      }
    }

    return search_result_posts;
  }

  // searchForFullText
  function searchForFullText(keyword, doSerach, doError) {
    // 索引数据的路径 searchindexUrl
    // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex/
    // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex1/
    // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex2.json
    // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex3.xml
    var searchindexUrl = "/assets/search/postsindex1/";
    var url = myblog.baseurl + searchindexUrl + '?t=' + myblog.buildAt;
    var expires = 0;

    if (!window.localStorage) {
      // 浏览器不支持 localstorage，将会每次都从服务端请求文章数据
      // get请求
      myblog.serverStorage.getForObject(url, function (postsData, httpStatus) {
        // success 时
        if (postsData) {
          // var postsData_sample = {
          //   "version": "v1.0.0",
          //   "buildAt": "20230515063617",
          //   "total_posts": "8",
          //   "posts": [
          //     {
          //       "title": "学习Liquid",
          //       "description": "",
          //       "content": "",
          //       "date": "Mon, 15 May 2023 00:00:00 +0000",
          //       "url": "/FakeZhangyueEinkAppStore/2023-05-15-%E5%AD%A6%E4%B9%A0Liquid/"
          //     }
          //   ]
          // }
          var totalPosts = postsData.total_posts;
          var posts = postsData.posts;
          let search_result_posts = searchExecution(keyword, posts);
          doSerach(search_result_posts);
        }
      }, function (httpStatus, error) {
        // error 时
        doError(error);
      });
    } else {
      // 会将第一次请求的文章数据直接存储到本地 localStorage，这相当于一个5M大小的针对于前端页面的数据库
      var key = myblog.webStorage_key_prefix + "posts";
      // 从中 localStorage 取出文章数据
      var postsData = myblog.webStorage.getItemByKey(key);
      if (postsData && postsData.buildAt == myblog.buildAt) {
        // 执行搜索
        var totalPosts = postsData.total_posts;
        var posts = postsData.posts;
        let search_result_posts = searchExecution(keyword, posts);
        doSerach(search_result_posts);
      } else {
        myblog.webStorage.removeItemByKey(key);
        // get请求
        myblog.serverStorage.getForObject(url, function (postsData, httpStatus) {
          // success 时
          if (postsData) {
            // 把文章数据存储到 localStorage
            myblog.webStorage.setItem(key, postsData);

            var totalPosts = postsData.total_posts;
            var posts = postsData.posts;
            let search_result_posts = searchExecution(keyword, posts);
            doSerach(search_result_posts);
          }
        }, function (httpStatus, error) {
          // error 时
          doError(error);
        });
      }
    }
  }

  // search
  function search(keyword) {
    // 样式 完全不透明
    document.querySelector('.search .icon-loading').style.opacity = 1;
    // 搜索
    searchForFullText(keyword, function (search_result_posts) {
      // doSerach
      if (search_result_posts) {
        // 样式 完全透明
        document.querySelector('.search .icon-loading').style.opacity = 0;
        // 渲染
        renderDom(search_result_posts);
      } else {
        // 样式 完全透明
        document.querySelector('.search .icon-loading').style.opacity = 0;
        // 渲染
        renderDom(null);
      }
    }, function (error) {
      // doError
      // 样式 完全透明
      document.querySelector('.search .icon-loading').style.opacity = 0;
      // 渲染
      renderDom(null);
    })
  }

  // renderDom
  function renderDom(showPosts) {
    let ul_list_search = document.querySelector('.search .list-search');
    if (showPosts) {
      // 渲染
      let ul = document.createElement("ul");
      ul.className = "list-search";
      for (let i = 0; i < showPosts.length; i++) {
        let post = showPosts[i];

        let title = post.title;
        let content = post.content;
        let date = post.date;
        let url = post.url;


        let p_title = document.createElement("p");
        p_title.className = "title";
        let text_node_title = document.createTextNode(title);
        p_title.appendChild(text_node_title);

        let p_content = document.createElement("p");
        p_content.className = "content";
        let text_node_content = document.createTextNode(content);
        p_content.appendChild(text_node_content);

        let a = document.createElement("a");
        a.setAttribute("href", url);
        a.appendChild(p_title);
        a.appendChild(p_content);

        let li = document.createElement('li');
        location.appendChild(a);
        // li.innerHTML = '<ul class="list-search">' +
        //   '<li hidden>' +
        //   '<a href="' + url + '">' +
        //   '<p class="title">' + title + '</p>' +
        //   '<p class="content">' + content + '</p>' +
        //   '</a>' +
        //   '</li>' +
        //   '</ul>';

        ul.appendChild(li);
      }

      let div_search = ul_list_search.parentNode;
      ul_list_search.parentNode.removeChild(ul_list_search);
      div_search.appendChild(ul);
    } else {
      ul_list_search.parentNode.removeChild(ul_list_search);
    }
  }

  let searchEngine = {
    init, searchForFullText
  };

  return searchEngine;
}

myblog.addEventListener = addEventListener;
myblog.webStorage_key_prefix = 'blog_' + myblog.author + '_' + myblog.baseurl + '_';
// 判断浏览器是否支持 Storage 存储对象
myblog.isSupportWebStorage = isSupportWebStorage();
// 使用 localStorage
myblog.webStorage = getWebStorage("sessionStorage");
myblog.serverStorage = getServerStorage();
myblog.searchEngine = getSearchEngine();
myblog.searchEngine.init(myblog);



function search(keyword) {
  let li = document.querySelectorAll('.search .list-search li')
  for (let i = 0; i < li.length; i++) {
    let dom_li = li[i]
    let dom_title = dom_li.querySelector('.title')
    let dom_content = dom_li.querySelector('.content')
    dom_title.innerHTML = title
    dom_content.innerHTML = ''
    if (hide) {
      dom_li.setAttribute('hidden', true)
    } else {
      dom_li.removeAttribute('hidden')
    }
  }
}

function parseTitle() {
  let arr = [];
  let doms = document.querySelectorAll('.search .list-search .title');
  for (let i = 0; i < doms.length; i++) {
    arr.push(doms[i].innerHTML);
  }
  return arr;
}


//////////////////////////////////////////////////////////////////////////////////////////
// var removeKey = document.getElementById("removeKey").value;
// removeKey.value = "";
// if (Object.prototype.toString.call(value) == '[object Object]') {

// }
// if (Object.prototype.toString.call(value) == '[object Array]') {

// }

// function retrieveUserData() {
//   let key = document.getElementById("retrieveKey").value;
//   console.log("retrive records");
//   let userData = localStorage.getItem(key); //searches for the key in localStorage
//   let paragraph = document.createElement("p");
//   let info = document.createTextNode(userData);
//   paragraph.appendChild(info);
//   let element = document.getElementById("userData");
//   element.appendChild(paragraph);
//   retrieveKey.value = "";
// }


// window.onload = function () {
//   document.getElementById("userForm").onsubmit = store;
//   document.getElementById("clearButton").onclick = deleteAllUserData;
//   document.getElementById("removeButton").onclick = removeUserData;
//   document.getElementById("retrieveButton").onclick = retrieveUserData;
// };

// module.exports = {
//   set,
//   localget: get1
// };



