const fs = require('fs');
const url = require('url');

const storeDir = __dirname + '/../../EinkAppStore/';

fs.watch(storeDir, { recursive: true }, (eventType, filename) => {
    console.log(`伪应用商店中的目录文件有变动：${eventType} ${filename}`);
});

// 伪应用商店
function fakeAppStoreService(request, response) {
    //
    let code = 0;
    let msg = "";
    let body = "";
    //
    let resStatusCode;
    let resContentType;
    let resData = "";

    const requestUrl = url.parse(request.url, true);
    const query = requestUrl.query;
    if (query.ca === 'Eink_AppStore.AppList') {
        // app列表分页数据查询地址“ca=Eink_AppStore.AppList”
        // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppList&page=1&size=7&categoryId=0
        let currentPage = query.page;
        let pageSize = query.size;
        let categoryId = query.categoryId;
        if (currentPage && pageSize && categoryId && currentPage > 0 && pageSize > 0 && categoryId >= 0) {
            let jsonFileName = 'AppList_AppInfo.json';
            // 读取文件AppList_AppInfo.json
            fs.readFile(storeDir + jsonFileName, 'utf8', function (err, data) {
                if (err) {
                    code = -1;
                    msg = "读取" + jsonFileName + "失败。" + err.message;
                    console.error(msg);
                    resStatusCode = 200;
                    resContentType = 'application/json;charset=utf-8';
                } else {
                    try {
                        // 分页
                        let AppList_AppInfo = JSON.parse(data);
                        let body_list = AppList_AppInfo.slice((currentPage - 1) * pageSize, currentPage * pageSize);
                        body_list = body_list.map(function (item, index, arr) {
                            return {
                                "id": item.id,
                                "name": item.name,
                                "icon": item.icon,
                                "appVersion": item.appVersion,
                                "appSize": item.appSize,
                                "appName": item.appName,
                                "appDesc": item.appDesc
                            };
                        });
                        let totalRecord = AppList_AppInfo.length;
                        let totalPage = Math.ceil(totalRecord / pageSize);
                        let body_page = {
                            "currentPage": parseInt(currentPage),
                            "pageSize": parseInt(pageSize),
                            "totalPage": totalPage,
                            "totalRecord": totalRecord
                        };
                        //
                        code = 0;
                        msg = "";
                        body = {
                            "list": body_list,
                            "page": body_page
                        };
                        console.log("读取" + jsonFileName + "完成。");
                        resStatusCode = 200;
                        resContentType = 'application/json';
                    } catch (err) {
                        code = -1;
                        msg = "读取" + jsonFileName + "失败。" + err.message;
                        console.error(msg);
                        resStatusCode = 200;
                        resContentType = 'application/json;charset=utf-8';
                    }
                }
                resData = JSON.stringify({
                    "code": code,
                    "msg": msg,
                    "body": body
                });

                // 响应
                response.writeHead(resStatusCode, { "Content-Type": resContentType });
                response.end(resData);
            });
        } else {
            resStatusCode = 200;
            resContentType = 'application/json;charset=utf-8';
            resData = '{"code":-1,"msg":"参数page、size、categoryId值只能是数字"}';

            // 响应
            response.writeHead(resStatusCode, { "Content-Type": resContentType });
            response.end(resData);
        }
    } else if (query.ca === 'Eink_AppStore.AppInfo') {
        // app详情数据查询地址“ca=Eink_AppStore.AppInfo”
        // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.AppInfo&appName=com.zhangyue.read.iReader.eink
        let appName = query.appName;
        if (appName && appName != "") {
            let jsonFileName = 'AppList_AppInfo.json';
            // 读取文件AppList_AppInfo.json
            fs.readFile(storeDir + jsonFileName, 'utf8', function (err, data) {
                if (err) {
                    code = -1;
                    msg = "读取" + jsonFileName + "失败。" + err.message;
                    console.error(msg);
                    resStatusCode = 200;
                    resContentType = 'text/plain; charset=utf-8';
                } else {
                    try {
                        // 查找appName
                        let AppList_AppInfo = JSON.parse(data);
                        let appInfo = AppList_AppInfo.find(function (item, index, arr) {
                            return appName === item.appName;
                        });
                        //
                        if (appInfo == undefined) {
                            code = -1;
                            msg = "参数appName值未找到";
                        } else {
                            code = 0;
                            msg = "";
                            body = appInfo;
                        }
                        console.log("读取" + jsonFileName + "完成。");
                        resStatusCode = 200;
                        resContentType = 'application/json';
                    } catch (err) {
                        code = -1;
                        msg = "读取" + jsonFileName + "失败。" + err.message;
                        console.error(msg);
                        resStatusCode = 200;
                        resContentType = 'text/plain; charset=utf-8';
                    }
                }
                let result = {
                    "code": code,
                    "msg": msg,
                    "body": body
                };
                resData = JSON.stringify(result);

                // 响应
                response.writeHead(resStatusCode, { "Content-Type": resContentType });
                response.end(resData);
            });
        } else {
            resStatusCode = 200;
            resContentType = 'application/json;charset=utf-8';
            resData = '{"code":-1,"msg":"参数appName值为空"}';

            // 响应
            response.writeHead(resStatusCode, { "Content-Type": resContentType });
            response.end(resData);
        }
    } else if (query.ca === 'Eink_AppStore.Category') {
        // app类别查询地址“ca=Eink_AppStore.Category”
        // http://ebook.zhangyue.com/zybook3/app/app.php?ca=Eink_AppStore.Category
        let jsonFileName = 'Category.json';
        // 读取文件Category.json
        fs.readFile(storeDir + jsonFileName, 'utf8', function (err, data) {
            if (err) {
                code = -1;
                msg = "读取" + jsonFileName + "失败。" + err.message;
                console.error(msg);
                resStatusCode = 200;
                resContentType = 'text/plain; charset=utf-8';
            } else {
                try {
                    //
                    let Category = JSON.parse(data);
                    //
                    code = 0;
                    msg = "";
                    body = Category;
                    console.log("读取" + jsonFileName + "完成。");
                    resStatusCode = 200;
                    resContentType = 'application/json';
                } catch (err) {
                    code = -1;
                    msg = "读取" + jsonFileName + "失败。" + err.message;
                    console.error(msg);
                    resStatusCode = 200;
                    resContentType = 'text/plain; charset=utf-8';
                }
            }
            let result = {
                "code": code,
                "msg": msg,
                "body": body
            };
            resData = JSON.stringify(result);

            // 响应
            response.writeHead(resStatusCode, { "Content-Type": resContentType });
            response.end(resData);
        });
    } else {
        // 未能匹配应用商店“ca = Eink_AppStore.***”格式的3种接口的请求
        resStatusCode = 200;
        resContentType = 'application/json;charset=utf-8';
        resData = '{"code":-1,"msg":"应用商店，参数ca值必须是\'ca=Eink_AppStore.***\'格式的3种接口"}';

        // 响应
        response.writeHead(resStatusCode, { "Content-Type": resContentType });
        response.end(resData);
    }
}

module.exports = fakeAppStoreService;
