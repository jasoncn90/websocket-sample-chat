"use strict";

var express = require('express');
var chat = express.Router();

chat.get('/', function(request, response, next) {
    response.sendfile('views/index.html');
});

module.exports = chat;