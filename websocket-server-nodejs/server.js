"use strict";

var server = require('http').createServer();
var express = require('express');
var app = express();    //main app
var port = 8080;

app.use(express.static(__dirname + '/public'));

//sub app for chat
var chat = require('./routes/chat');
app.use('/chat', chat);

server.on('request', app);
server.listen(port, function() {
    console.log('Listening on ' + server.address().port);
});

var wsUtil = require('./utils/wsUtil');
wsUtil.startWebSocketServer();
