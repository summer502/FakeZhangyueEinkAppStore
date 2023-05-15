myblog.addEventListener=addEventListener;
// 判断浏览器是否支持 Storage 存储对象
myblog.isSupportWebStorage = isSupportWebStorage();
myblog.webStorage_key_prefix='blog_'+ myblog.author + '_' + myblog.baseurl + '_';
// 使用 localStorage
myblog.webStorage = getWebStorage("localStorage");
myblog.serverStorage = getServerStorage();











function searchEngine() {
  let myblog;

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

  function init(myBlog) {
    // 搜索输入框
    let searchInputElement = document.getElementById('search-input');
    if (!searchInputElement) {
      return;
    } else {
      myblog = myBlog;
      myblog.searchInputElement = searchInputElement;
    }

    if (myblog.searchInputElement) {
      myblog.addEventListener(myblog.searchInputElement, 'input', function (event) {
        if (!inputLock) {
          search(event.target.value)
        }
      })

      myblog.addEventListener(searchInputElement, 'compositionstart', function (event) {
        inputLock = true
      })

      myblog.addEventListener(searchInputElement, 'compositionend', function (event) {
        inputLock = false
        search(event.target.value)
      })
    }
  }

  let searchEngine = {
    init, searchForFullText
  };

  return searchEngine;
}

// search
function search(keyword) {
  // 样式 完全不透明
  document.querySelector('.search .icon-loading').style.opacity = 1;
  // 搜索
  searchForFullText(keyword);
}

// doError
function doError(error) {
  console.error('全文检索数据加载失败...')
  callback ? callback(null) : ''
}

// doSerach
function doSerach(search_result_posts) {
  //   "posts": [
        //     {
        //       "title": "学习Liquid",
        //       "description": "",
        //       "content": "",
        //       "date": "Mon, 15 May 2023 00:00:00 +0000",
        //       "url": "/FakeZhangyueEinkAppStore/2023-05-15-%E5%AD%A6%E4%B9%A0Liquid/"
        //     }
        //   ]


  // 样式 完全透明
  document.querySelector('.search .icon-loading').style.opacity = 0;
  // 渲染
  renderDom(search_result_posts);
}

// renderDom
function renderDom(showPosts) {
  if (showPosts) {
// 渲染

  }
}



















function callback(data) {
  titles = parseTitle();
  contents = parseContent(data);
  search(input.value);
}

function search(keyword) {

  // <>& 替换
  keyword = trimKeyword(keyword);

  let doms = document.querySelectorAll('.search .list-search li')
  for (let i = 0; i < doms.length; i++) {
    let title = titles[i]
    let content = contents[i]
    let dom_li = doms[i]
    let dom_title = dom_li.querySelector('.title')
    let dom_content = dom_li.querySelector('.content')

    dom_title.innerHTML = title
    dom_content.innerHTML = ''

    // 空字符隐藏
    if (keyword == '') {
      dom_li.setAttribute('hidden', true)
      continue
    }
    let hide = true

    let hit = false;
    let search_result_posts = [];
    let showStrlength = 100;
    let showTitle;
    let showContent;

    let keyword_patt_i = new RegExp(blog.encodeRegChar(keyword), 'i');
    let keyword_patt_g_i = new RegExp(blog.encodeRegChar(keyword), 'gi');

    // 检测 title 是否匹配正则模式
    if (keyword_patt_g_i.test(title)) {
      // title 全局替换
      showTitle = title.replace(keyword_patt_g_i, '<span class="hint">' + keyword + '</span>');
      hide = false
      hit = true;
    } else {
      showTitle = title;
    }

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
      hide = false
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

    if (hit) {
      let post = {
        "title": showTitle,
        "content": showContent,
        "date": date,
        "url": url
      };
      search_result_posts.push(post);
    }

    dom_title.innerHTML = showTitle;
    dom_content.innerHTML = showContent;
    if (hide) {
      dom_li.setAttribute('hidden', true)
    } else {
      dom_li.removeAttribute('hidden')
    }
  }
}




// 标题等信息
let titles = [];
// 正文内容
let contents = [];
// 解析 json 格式的 posts 对象
function parseTitleForJson(posts) {
  var posts_sample = [
    {
      "title": "{{ title }}",
      "description": "{{ description }}",
      "content": "{{ content }}",
      "date": "{{ date }}",
      "url": "{{ url }}"
    }];
  let arr = [];
  for (let i = 0; i < posts.length; i++) {
    arr.push(posts[i].title);
  }
  return arr;
}

function parseTitle() {
  let arr = [];
  let doms = document.querySelectorAll('.search .list-search .title');
  for (let i = 0; i < doms.length; i++) {
    arr.push(doms[i].innerHTML);
  }
  return arr;
}

function parseContent(data) {
  let arr = []
  let root = document.createElement('div')
  root.innerHTML = data
  let doms = root.querySelectorAll('li')
  for (let i = 0; i < doms.length; i++) {
    arr.push(doms[i].innerHTML)
  }
  return arr
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



