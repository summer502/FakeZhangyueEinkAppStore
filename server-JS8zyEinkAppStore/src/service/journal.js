const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const logsPath = path.join(__dirname, '../../logs');
const accessFilePath = path.join(__dirname, '../../logs/access.log');
const errorFilePath = path.join(__dirname, '../../logs/error.log');

// 压缩 filePath 文件为 filePath.gz
function compress(filePath) {
    try {
        fs.createReadStream(filePath)
            .pipe(zlib.createGzip())
            .pipe(fs.createWriteStream(filePath + '.gz'));
    } catch (err) {
        console.error("======compress=========", err.name, err.code, err.message);
    } finally {

    }
}

// 解压 filePath.gz 文件为 filePath
function decompress(filePath) {
    fs.createReadStream(filePath + '.gz')
        .pipe(zlib.createGunzip())
        .pipe(fs.createWriteStream(filePath));
}

//
function writeContent(filePath, content) {
    fs.access(logsPath, fs.constants.F_OK, err => {
        console.error('目录不存在', err);
    })
    fs.access(filePath, fs.constants.F_OK | fs.constants.W_OK, err => {
        console.error('文件不存在、文件不可写', err);
    })

    content += "\r\n";
    fs.appendFile(filePath, content, (err) => {
        if (err) {
            console.error(err);
            return;
        }
    });
    // fs.appendFileSync(filePath, content);
}

//
function writeContentByStream(filePath, content) {
    content += "\r\n";
    var ws = fs.createWriteStream(filePath, {
        flags: 'a'
    }).on('error', function (err) {
        console.error(err.name, err.stack);
    });
    ws.on('finish', function () {
        // console.log("====write log======完成=====")
    });
    let result = ws.write(content, 'UTF8');
    if (!result) {
        console.log("====write log=======缓存队列满了====")
    }
    ws.end();
}

function writeAccessLog(logStr) {
    writeContentByStream(accessFilePath, logStr);
}

function writeErrorLog(logStr) {
    writeContentByStream(errorFilePath, logStr);
}

module.exports = {
    writeAccessLog,
    writeErrorLog
}
