// const js8AppStoreServer = require('src/JS8zyEinkAppStoreServer.js');
// todo 要重构，后面有空再弄
const http = require('http');
const port = '3000';

let app = http.createServer(function (req, res) {
 res.end('Hello World')
})

app.listen(port, () => {
	console.log('listenPort:', port)
})