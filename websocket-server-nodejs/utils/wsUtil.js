"use strict";

var wsport = 9003;  //ws server port
var ticket = 'test' //auth ticket
var url = require('url');
exports.startWebSocketServer = function() {
    var WebSocketServer = require('ws').Server;
    var wss = new WebSocketServer({ port: wsport });
    var names = new Array();
    wss.on('connection', function connection(ws) {
        var location = url.parse(ws.upgradeReq.url, true);
        var token = location.query.ticket;
        var name = location.query.name;
        if (token != ticket) {
            console.log('auth failed');
            ws.close(3000, 'auth failed');
        } else if (names.indexOf(name) > -1) {
            console.log('name %s already exist!', name);
            ws.close(3001, name + ' already exist!');
        } else {
            console.log('auth success. %s connected ', name);
            ws.name = name;
            names.push(name);
            wss.clients.forEach(function each(client) {
                client.send(name + ' joined the chat.');
            });
        }
        ws.on('message', function incoming(message, flags, data1, data3) {
            console.log('received: %s from %s', message, ws.name);
            wss.clients.forEach(function each(client) {
                if (client.name != ws.name) {
                    client.send(ws.name + ': ' + message);
                }
            });
        });

        ws.on('close', function(data) {
            var index = names.indexOf(ws.name);
            if (index > -1) {
                names.splice(index, 1);
                wss.clients.forEach(function each(client) {
                    console.log(ws.name + ' left the chat.');
                    if (client.readyState == require('ws').OPEN) {
                        client.send(ws.name + ' left the chat.');
                    }
                });
            }
        });
    });
}
