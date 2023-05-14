
myblog.isSupportWebStorage = isSupportWebStorage();
// 使用 localStorage
myblog.webStorage = getWebStorage("localStorage");
myblog.serverStorage = getServerStorage();

// search
function  searchForFullText(keyword) {
  var posts = [{ title, content }, {}];
  var url = myblog.baseurl + '/assets/search/postsindex.xml?t=' + myblog.buildAt;

  if (!window.localStorage) {
    // 浏览器不支持 localstorage，将会每次都从服务端请求文章数据
    myblog.serverStorage.getForObject(
      url, function (posts) {
        // success 时
        doSerach(postsData);
      }, function (data) {
        // error 时
        console.error('全文检索数据加载失败...')
        callback ? callback(null) : ''
      }
    );
  } else {
    // 会将第一次请求的文章数据直接存储到本地 localStorage，这相当于一个5M大小的针对于前端页面的数据库
    var key = webStorage_key_prefix + "posts";
    var expires = 0;

    var postsData = myblog.webStorage.getItemByKey(key);
    if (postsData && postsData.buildAt == myblog.buildAt) {
      doSerach(postsData);
    } else {
      myblog.webStorage.removeItemByKey(key);

      myblog.serverStorage.getForObject(
        url, function (posts) {
          // success 时
          myblog.webStorage.setItem(key, posts);
          doSerach(postsData);
        }, function (data) {
          // error 时
          console.error('全文检索数据加载失败...')
          callback ? callback(null) : ''
        }
      );
    }
  }
}

function doSerach(postsData) {
  var version = postsData.version;
  var buildAt = postsData.buildAt;
  var total_posts = postsData.total_posts;
  var posts = postsData.posts;
  var postData1 = {
    "version": "{{ version }}",
    "buildAt": "{{ buildAt }}",
    "total_posts": "{{ paginator.total_posts }}",
    "posts": [
      {
        "title": "{{ title }}",
        "description": "{{ description }}",
        "content": "{{ content }}",
        "date": "{{ date }}",
        "url": "{{ url }}"
      }

    ]
  };
  // 样式
  document.querySelector('.page-search .icon-loading').style.opacity = 0
  //
  callback ? callback(postsData) : ''
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



