// 判断浏览器是否支持 Storage 存储对象
myblog.isSupportWebStorage = isSupportWebStorage();
// 使用 localStorage
myblog.webStorage = getWebStorage("localStorage");
myblog.serverStorage = getServerStorage();

// search
function searchForFullText(keyword) {
  // 索引数据的路径 searchindexUrl
  // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex/
  // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex1/
  // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex2.json
  // https://summer502.github.io/FakeZhangyueEinkAppStore/assets/search/postsindex3.xml
  var searchindexUrl = "/assets/search/postsindex1/";
  var url = myblog.baseurl + searchindexUrl + '?t=' + myblog.buildAt;
  if (!window.localStorage) {
    // 浏览器不支持 localstorage，将会每次都从服务端请求文章数据
    // get请求
    myblog.serverStorage.getForObject(url, function (postsData) {
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
        //       "url": "/2023-05-15-%E5%AD%A6%E4%B9%A0Liquid/"
        //     }
        //   ]
        // }
        doSerach(keyword, postsData.posts, postsData.total_posts);
      }
    }, function (error) {
      // error 时
      doError(error);
    });
  } else {
    // 会将第一次请求的文章数据直接存储到本地 localStorage，这相当于一个5M大小的针对于前端页面的数据库
    var key = webStorage_key_prefix + "posts";
    var expires = 0;
    // 从中 localStorage 取出文章数据
    var postsData = myblog.webStorage.getItemByKey(key);
    if (postsData && postsData.buildAt == myblog.buildAt) {
      doSerach(keyword, postsData.posts, postsData.total_posts);
    } else {
      myblog.webStorage.removeItemByKey(key);
      // get请求
      myblog.serverStorage.getForObject(url, function (postsData) {
        // success 时
        if (postsData) {
          // 把文章数据存储到 localStorage
          myblog.webStorage.setItem(key, postsData);

          doSerach(keyword, postsData.posts, postsData.total_posts);
        }
      }, function (error) {
        // error 时
        doError(error);
      });
    }
  }
}
// doSerach
function doSerach(keyword, posts, totalPosts) {
  // var posts_sample =[
  // {
  //   "title": "{{ title }}",
  //   "description": "{{ description }}",
  //   "content": "{{ content }}",
  //   "date": "{{ date }}",
  //   "url": "{{ url }}"
  // }];
  // 样式
  document.querySelector('.search .icon-loading').style.opacity = 0;
  // 执行搜索
  callback ? callback(posts) : ''
}
// 输入框
let input = document.getElementById('search-input');

// 非搜索页面
if (!input) {
  return
} else {
  blog.addEvent(input, 'input', function (event) {
    if (!inputLock) {
      search(event.target.value)
    }
  })

  blog.addEvent(input, 'compositionstart', function (event) {
    inputLock = true
  })

  blog.addEvent(input, 'compositionend', function (event) {
    inputLock = false
    search(event.target.value)
  })
}


function callback(data) {
  titles = parseTitle();
  contents = parseContent(data);
  search(input.value);
}
// trimKeyword
function trimKeyword(keyword) {
  // <>& 替换
  keyword = blog.trim(keyword)
  keyword = keyword.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/&/g, '&amp;')
  return keyword;
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
    let keyword_patt_i = new RegExp(blog.encodeRegChar(keyword), 'i');
    let keyword_patt_g_i = new RegExp(blog.encodeRegChar(keyword), 'gi');

    // 检测 title 是否匹配模式
    if (keyword_patt_g_i.test(title)) {
      // title 全局替换
      dom_title.innerHTML = title.replace(keyword_patt_g_i, '<span class="hint">' + keyword + '</span>');
      hide = false
    }

    let showStrlength = 100;
    let showContent;
    // 检索 content 中的 keyword
    let result = keyword_patt_i.exec(content);
    if (result) {
      // 检索时找到第一个 keyword 的位置
      let index = result.index;
      
      let begin = (index - 10) < 0 ? 0 : index - 10;
      let end = begin + showStrlength;
      showContent = content.substring(begin, end);
      // content 全局替换
      showContent = showContent.replace(keyword_patt_g_i, '<span class="hint">' + keyword + '</span>') + '...';
      hide = false
    } else {
      // 内容未命中标题命中，内容直接展示前100个字符
      if (!hide && content) {
        let begin = 0;
        let end = begin + showStrlength;
        showContent = content.substring(begin, end) + '...';
      }
    }
    dom_content.innerHTML = showContent;

    if (hide) {
      dom_li.setAttribute('hidden', true)
    } else {
      dom_li.removeAttribute('hidden')
    }
  }
}
// doError
function doError(error) {
  console.error('全文检索数据加载失败...')
  callback ? callback(null) : ''
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



